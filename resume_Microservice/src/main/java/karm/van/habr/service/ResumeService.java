package karm.van.habr.service;

import karm.van.habr.entity.ImageResume;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import karm.van.habr.exceptions.ImageTroubleException;
import karm.van.habr.repo.ImageResumeRepo;
import karm.van.habr.repo.MyUserRepo;
import karm.van.habr.repo.ResumeRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {
    private final ResumeRepo resumeRepo;
    private final MyUserRepo myUserRepo;
    private final ImageResumeRepo imageResumeRepo;
    private final ImageCompressionService imageCompressionService;

    @Transactional(rollbackFor = {ImageTroubleException.class, UsernameNotFoundException.class, IOException.class})
    public void createResume(Authentication authentication, String title, String description, MultipartFile[] files) throws ImageTroubleException, UsernameNotFoundException, IOException {
        Optional<MyUser> opt_user = myUserRepo.findByName(authentication.getName());

        if (opt_user.isEmpty()){
            throw new UsernameNotFoundException("Пользователь с таким именем не найден");
        } else if (files.length > 4){
            throw new ImageTroubleException("Слишком много изображений");
        }

        MyUser user = opt_user.get();

        Resume resume = Resume.builder()
                .title(title)
                .description(description)
                .author(user)
                .createdAt(LocalDate.now())
                .build();

        resumeRepo.saveAndFlush(resume);

        imageSave(files,resume);

    }

    private void imageSave(MultipartFile[] files,Resume resume) throws ImageTroubleException {
        ExecutorService executorService = Executors.newFixedThreadPool(files.length);

        List<Callable<byte[]>> tasks = getCallableList(files);

        try {
            List<Future<byte[]>> results = executorService.invokeAll(tasks);

            for (Future<byte[]> result : results) {
                byte[] compressedImage = result.get();
                ImageResume imageResume = ImageResume.builder()
                        .resume(resume)
                        .image(compressedImage)
                        .imageType(files[results.indexOf(result)].getContentType())
                        .build();
                imageResumeRepo.save(imageResume);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new ImageTroubleException(e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }

    private List<Callable<byte[]>> getCallableList(MultipartFile[] files) {
        List<Callable<byte[]>> tasks = new ArrayList<>();

        for (MultipartFile file : files) {
            tasks.add(() -> {
                byte[] compressedImage = imageCompressionService.compressImage(file.getBytes(), file.getContentType());
                if (compressedImage == null) {
                    throw new RuntimeException("Какая-то проблема с обработкой изображений. Приносим свои извинения. Попробуйте перезагрузить страницу и предоставить новые");
                }
                return compressedImage;
            });
        }
        return tasks;
    }


    @Transactional
    public ImageResume getImage(Long resumeId,int imageId){
        Optional<Resume> resume = resumeRepo.findById(resumeId);
        if (resume.isEmpty()){
            return null;
        }
        List<ImageResume> images = imageResumeRepo.findByResume(resume.get());
        return images.get(imageId);

    }

    public int getPaginationQuantity(){
        double resumesQuantity = resumeRepo.findAll().size();
        double result = resumesQuantity/6;
        log.info(String.valueOf(result));
        return result>0?(int)Math.ceil(result):1;
    }


    //@Cacheable(cacheNames = {"listOfResumes"},key = "{#offset,#limit}")
    public Page<Resume> getAllResume(int offset, int limit){
        return resumeRepo.findAll(PageRequest.of(offset,limit));
    }

    //TODO: Разобраться как отсутствие этого бесполезного метода ломает мне программу
    @Transactional
    public List<ImageResume> getAllImages(){return imageResumeRepo.findAll();}

}
