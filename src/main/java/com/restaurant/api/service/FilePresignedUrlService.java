package com.restaurant.api.service;

import com.restaurant.api.config.AwsProperties;
import com.restaurant.api.dto.PreSignedUrlDto;
import com.restaurant.api.entity.File;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.file.FileRepository;
import com.restaurant.api.util.FileAttachmentUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilePresignedUrlService {

    private final FileRepository fileRepository;
    private final AwsProperties awsProperties;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;



    public String getPublicUrl (String fileAttachmentPatch){
        String bucketName = awsProperties.getBucketName();
        String region = awsProperties.getRegion();

        return  "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileAttachmentPatch;
    }

    @Transactional
    public List<PreSignedUrlDto.Response> generatePreSignedUrls(List<PreSignedUrlDto.Request> requestList, long expirationMinutes){

        String bucketName = awsProperties.getBucketName();
        return requestList.stream()
                .map(request -> {
                    String fileAttachmentName = request.fileName();
                    String folderName = request.folderName();

                    String uniqueFileName = FileAttachmentUtil.generateUniqueFileNameWithTimeStamp(fileAttachmentName);
                    String filePath = Paths.get(folderName, uniqueFileName).toString();

                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(filePath)
                            .build();

                    PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(builder -> builder
                            .putObjectRequest(putObjectRequest)
                            .signatureDuration(Duration.ofMinutes(expirationMinutes)));

                    // 정적 URL 생성
                    String publicUrl = this.getPublicUrl(filePath);

                    // DB에 파일 정보 저장
                    File file = new File(publicUrl, request.type());

                    fileRepository.save(file);

                    // 결과 반환
                    return new PreSignedUrlDto.Response(
                            file.getCode(),
                            publicUrl,
                            presignedPutObjectRequest.url().toString()
                    );
                })
                .collect(Collectors.toList());
    }

    public void deleteFileFromS3(String fileAttachmentName) {
        String bucketName = awsProperties.getBucketName();

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileAttachmentName)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    @Transactional(readOnly = true)
    public File getById(String id) {
        return fileRepository.findByCode(id).orElseThrow(() -> new ApiException(ErrorCode.FILE_NOT_FOUND));
    }

//    public List<File> getFileListByCodeIn(List<String> idList) {
//        if (idList == null || idList.isEmpty()) {
//            throw new RuntimeException("idList is null or empty");
//        }
//
//        List<File> fileList = fileRepository.f(idList);
//        if (fileList.isEmpty()) {
//            throw new ApiException(ErrorCode.FILE_NOT_FOUND);
//        }
//
//        // Check if all requested IDs were found
//        if (fileList.size() != idList.size()) {
//            throw new RuntimeException("inut file is larger than the exsting file list");
//        }
//
//        return fileList;
//    }

//    @Transactional(readOnly = true)
//    public List<File> getAllByIdList(List<String> fileAttachmentIdList) {
//        List<File> fileAttachmentList = fileRepository.findAllByIdIn(fileAttachmentIdList);
//
//        if (fileAttachmentIdList.size() != fileAttachmentList.size()) {
//            throw new ApiException(ErrorCode.FILE_ATTACHMENT_ID_INCLUDE_INVALID_ID);
//        }
//
//        return fileAttachmentList;
//    }
//
//    @Transactional
//    public List<FileAttachment> saveAll(List<FileAttachment> fileAttachmentList) {
//        return fileAttachmentRepository.saveAll(fileAttachmentList);
//    }
//
//    @Transactional
//    public void updateSuccessTrueIfExist(FileAttachment fileAttachment) {
//        if (Objects.nonNull(fileAttachment)) {
//            fileAttachment.isSuccess(Boolean.TRUE);
//            fileAttachmentRepository.save(fileAttachment);
//        }
//    }
//
//    @Transactional
//    public void updateSuccessFalseIfExist(FileAttachment fileAttachment) {
//        if (Objects.nonNull(fileAttachment)) {
//            fileAttachment.isSuccess(Boolean.FALSE);
//            fileAttachmentRepository.save(fileAttachment);
//        }
//    }

}
