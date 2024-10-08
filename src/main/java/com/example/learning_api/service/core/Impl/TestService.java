package com.example.learning_api.service.core.Impl;

import com.example.learning_api.constant.CloudinaryConstant;
import com.example.learning_api.constant.ErrorConstant;
import com.example.learning_api.dto.common.QuestionAnswersDTO;
import com.example.learning_api.dto.request.test.CreateTestRequest;
import com.example.learning_api.dto.request.test.ImportTestRequest;
import com.example.learning_api.dto.request.test.TestSubmitRequest;
import com.example.learning_api.dto.request.test.UpdateTestRequest;
import com.example.learning_api.dto.response.CloudinaryUploadResponse;
import com.example.learning_api.dto.response.question.GetQuestionsResponse;
import com.example.learning_api.dto.response.test.*;
import com.example.learning_api.entity.sql.database.*;
import com.example.learning_api.enums.*;
import com.example.learning_api.model.CustomException;
import com.example.learning_api.repository.database.*;
import com.example.learning_api.service.common.CloudinaryService;
import com.example.learning_api.service.common.ModelMapperService;
import com.example.learning_api.service.core.ITestService;
import com.example.learning_api.utils.ImageUtils;
import com.example.learning_api.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService implements ITestService {
    private final ModelMapperService modelMapperService;
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final ClassRoomRepository classRoomRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CloudinaryService cloudinaryService;
    private final StudentAnswersRepository studentAnswersRepository;
    private final TeacherRepository teacherRepository;
    private final StudentAnswersRepository studentAnswerRepository;
    private final FileRepository fileRepository;
    @Override
    public CreateTestResponse createTest(CreateTestRequest request) {
        validateCreateTestRequest(request);
        TestEntity testEntity = createTestEntity(request);
        FileEntity fileEntity = createFileEntity(request, testEntity);

        testRepository.save(testEntity);
        fileRepository.save(fileEntity);

        return createTestResponse(testEntity, fileEntity);
    }

    private void validateCreateTestRequest(CreateTestRequest request) {
        if (request.getTeacherId() == null) {
            throw new IllegalArgumentException("TeacherID is required");
        }
        TeacherEntity teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        if (request.getClassroomId() == null) {
            throw new IllegalArgumentException("ClassroomId is required");
        }
        if (classRoomRepository.findById(request.getClassroomId()).isEmpty()) {
            throw new IllegalArgumentException("ClassroomId is not found");
        }

        if (request.getAttemptLimit() == null) {
            throw new IllegalArgumentException("AttemptLimit is required");
        }
    }

    private TestEntity createTestEntity(CreateTestRequest request) {
        TestEntity testEntity = modelMapperService.mapClass(request, TestEntity.class);
        testEntity.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        testEntity.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
        return testEntity;
    }

    private FileEntity createFileEntity(CreateTestRequest request, TestEntity testEntity) {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setOwnerType(FileOwnerType.TEST);
        fileEntity.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        fileEntity.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

        if (request.getSource() != null) {
            processAndUploadImage(request, fileEntity);
        }

        fileEntity.setOwnerId(testEntity.getId());
        return fileEntity;
    }

    private void processAndUploadImage(CreateTestRequest request, FileEntity fileEntity) {
        if (!ImageUtils.isValidImageFile(request.getSource())) {
            throw new CustomException(ErrorConstant.IMAGE_INVALID);
        }

        try {
            byte[] originalImage = request.getSource().getBytes();
            byte[] resizedImage = ImageUtils.resizeImage(originalImage, 200, 200);
            CloudinaryUploadResponse imageUploaded = cloudinaryService.uploadFileToFolder(
                    CloudinaryConstant.CLASSROOM_PATH,
                    StringUtils.generateFileName(request.getName(), "tests"),
                    resizedImage,
                    "image"
            );

            fileEntity.setUrl(imageUploaded.getUrl());
            fileEntity.setExtension("jpg");
            fileEntity.setName(request.getSource().getOriginalFilename());

            fileEntity.setSize(String.valueOf(request.getSource().getSize()));
            fileEntity.setCreatedAt(String.valueOf(System.currentTimeMillis()));
            fileEntity.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

            fileEntity.setType("image");
        } catch (IOException e) {
            throw new CustomException("Failed to process image", e.toString());
        }
    }

    private CreateTestResponse createTestResponse(TestEntity testEntity, FileEntity fileEntity) {
        CreateTestResponse response = new CreateTestResponse();
        // Set all the fields from testEntity and fileEntity
        response.setTeacherId(testEntity.getTeacherId());
        response.setCreatedAt(testEntity.getCreatedAt());
        response.setDescription(testEntity.getDescription());
        response.setDuration(testEntity.getDuration());
        response.setId(testEntity.getId());
        response.setSource(fileEntity.getUrl());
        response.setName(testEntity.getName());
        response.setUpdatedAt(testEntity.getUpdatedAt());
        response.setStartTime(testEntity.getStartTime());
        response.setEndTime(testEntity.getEndTime());
        response.setClassroomId(testEntity.getClassroomId());
        response.setShowResultType(testEntity.getShowResultType().name());
        response.setStatus(testEntity.getStatus().name());
        response.setAttemptLimit(testEntity.getAttemptLimit() != null ? testEntity.getAttemptLimit() : 1);
        return response;
    }
    @Override
    public void updateTest(UpdateTestRequest body) {
        try{
            TestEntity testEntity = testRepository.findById(body.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Test not found"));
            if (body.getId()==null){
                throw new IllegalArgumentException("TestId is required");
            }
            if (testEntity==null){
                throw new IllegalArgumentException("TestId is not found");
            }
            testEntity.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
            if (body.getName()!=null){
                testEntity.setName(body.getName());
            }
            if (body.getDescription()!=null){
                testEntity.setDescription(body.getDescription());
            }
            if (body.getDuration()!=0){
                testEntity.setDuration(body.getDuration());
            }
            if (body.getSource()!=null){
                if (!ImageUtils.isValidImageFile(body.getSource())) {
                    throw new CustomException(ErrorConstant.IMAGE_INVALID);
                }
                fileRepository.deleteByOwnerIdAndOwnerType(testEntity.getId(), FileOwnerType.TEST.name());
                byte[] originalImage = new byte[0];
                originalImage = body.getSource().getBytes();
                byte[] newImage = ImageUtils.resizeImage(originalImage, 200, 200);
                CloudinaryUploadResponse imageUploaded = cloudinaryService.uploadFileToFolder(
                        CloudinaryConstant.CLASSROOM_PATH,
                        StringUtils.generateFileName(body.getName(), "tests"),
                        newImage,
                        "image"
                );
                FileEntity fileEntity = new FileEntity();
                fileEntity.setUrl(imageUploaded.getUrl());
                fileEntity.setType("image");
                fileEntity.setOwnerId(testEntity.getId());
                fileEntity.setOwnerType(FileOwnerType.TEST);
                fileEntity.setCreatedAt(String.valueOf(System.currentTimeMillis()));
                fileEntity.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
                fileRepository.save(fileEntity);
            }
            if (body.getImage()!=null){
                testEntity.setStartTime(body.getStartTime());
            }
            if (body.getStartTime()!=null){
                testEntity.setStartTime(body.getStartTime());
            }
            if (body.getEndTime()!=null){
                testEntity.setEndTime(body.getEndTime());
            }
            if (body.getShowResultType()!=null){
                testEntity.setShowResultType(TestShowResultType.valueOf(body.getShowResultType()));
            }
            if(body.getStatus()!=null){
                testEntity.setStatus(TestStatus.valueOf(body.getStatus()));
            }
            if (body.getAttemptLimit()!=null){
                testEntity.setAttemptLimit(body.getAttemptLimit());
            }

            testRepository.save(testEntity);
        }
        catch (Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }

    }

    @Override
    public void deleteTest(String id) {
        try{
            TestEntity testEntity = testRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Test not found"));
            List<FileEntity> fileEntities = fileRepository.findByOwnerIdAndOwnerType(testEntity.getId(), FileOwnerType.TEST.name());
            for (FileEntity fileEntity : fileEntities){
                cloudinaryService.deleteImage(fileEntity.getUrl());
                fileEntities.remove(fileEntity);
            }
            List<QuestionEntity> questionEntities = questionRepository.findByTestId(id);
            for (QuestionEntity questionEntity : questionEntities){
                answerRepository.deleteByQuestionId(questionEntity.getId());
            }
            questionRepository.deleteByTestId(id);
            testRepository.deleteById(id);
            testResultRepository.deleteByTestId(id);
        }
        catch (Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }

    }

    @Override
    public GetTestsResponse getTests(int page, int size, String search) {
        try{
            Pageable pageAble = PageRequest.of(page, size);
            Page<TestEntity> testEntities = testRepository.findByNameContaining(search, pageAble);
            GetTestsResponse resData = new GetTestsResponse();
            List<GetTestsResponse.TestResponse> testResponses = new ArrayList<>();
            for (TestEntity testEntity : testEntities){
                GetTestsResponse.TestResponse testResponse = modelMapperService.mapClass(testEntity, GetTestsResponse.TestResponse.class);
                testResponse.setSource(fileRepository.findByOwnerIdAndOwnerType(testEntity.getId(), FileOwnerType.TEST.name())
                        .stream()
                        .findFirst()
                        .orElse(null));
                if (testEntity.getAttemptLimit()==null){
                    testResponse.setAttemptLimit(1);
                }
                else{
                    testResponse.setAttemptLimit(testEntity.getAttemptLimit());
                }
                testResponses.add(testResponse);
            }
            resData.setTests(testResponses);
            resData.setTotalElements(testEntities.getTotalElements());
            resData.setTotalPage(testEntities.getTotalPages());
            return resData;
        }
        catch (Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    @Override
    public void importTest(ImportTestRequest body) {
        try {
            String fileContent = extractFileContent(body);
            List<QuestionEntity> questions = parseQuestions(fileContent, body.getTestId());
            saveQuestions(questions);
        } catch (IOException e) {
            throw new CustomException(ErrorConstant.FILE_PROCESSING_ERROR, e.toString());
        } catch (Exception e) {
            throw new CustomException(ErrorConstant.IMPORT_TEST_ERROR, e.getMessage());
        }
    }

    private String extractFileContent(ImportTestRequest body) throws IOException {
        if (body.getType() == ImportType.FILE) {
            return extractContentFromFile(body.getFile());
        } else {
            return body.getText();
        }
    }

    private String extractContentFromFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);

        switch (fileExtension) {
            case "pdf":
                return extractContentFromPdf(file);
            case "docx":
                return extractContentFromDocx(file);
            default:
                throw new CustomException(ErrorConstant.FILE_INVALID);
        }
    }

    private String getFileExtension(String fileName) {
        return Optional.ofNullable(fileName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.lastIndexOf(".") + 1).toLowerCase())
                .orElseThrow(() -> new CustomException(ErrorConstant.FILE_INVALID));
    }

    private String extractContentFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractContentFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            return document.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }

    private List<QuestionEntity> parseQuestions(String fileContent, String testId) {
        Pattern questionPattern = Pattern.compile("Câu\\s+(\\d+)\\s*:\\s*([^\\n]+)\\n([\\s\\S]+?)(?=\\nCâu\\s+\\d+\\s*:|$)");
        Matcher questionMatcher = questionPattern.matcher(fileContent);
        List<QuestionEntity> questions = new ArrayList<>();

        while (questionMatcher.find()) {
            String questionText = questionMatcher.group(2).trim();
            String answerGroup = questionMatcher.group(3);

            QuestionEntity question = createQuestion(questionText, testId);
            List<AnswerEntity> answers = parseAnswers(answerGroup, question.getId());
            question.setAnswers(answers);
            questions.add(question);
        }

        return questions;
    }

    private QuestionEntity createQuestion(String questionText, String testId) {
        QuestionEntity question = new QuestionEntity();
        question.setContent(questionText);
        question.setTestId(testId);
        question.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        question.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
        return question;
    }

    private List<AnswerEntity> parseAnswers(String answerGroup, String questionId) {
        Pattern answerPattern = Pattern.compile("\\b([A-D])\\.\\s*(.+?)(\\*?)(?=(?:\\n[A-D]\\.|$))", Pattern.DOTALL);
        Matcher answerMatcher = answerPattern.matcher(answerGroup);
        List<AnswerEntity> answers = new ArrayList<>();

        while (answerMatcher.find()) {
            String answerText = answerMatcher.group(2).trim();
            boolean isCorrect = !answerMatcher.group(3).isEmpty();

            AnswerEntity answer = createAnswer(answerText, questionId, isCorrect);
            answers.add(answer);
        }

        return answers;
    }

    private AnswerEntity createAnswer(String answerText, String questionId, boolean isCorrect) {
        AnswerEntity answer = new AnswerEntity();
        answer.setContent(answerText);
        answer.setQuestionId(questionId);
        answer.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        answer.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
        answer.setCorrect(isCorrect);
        return answer;
    }

    private void saveQuestions(List<QuestionEntity> questions) {
        for (QuestionEntity question : questions) {
            QuestionEntity savedQuestion = questionRepository.save(question);
            for (AnswerEntity answer : question.getAnswers()) {
                answer.setQuestionId(savedQuestion.getId());
                answerRepository.save(answer);
            }
        }
    }


    @Override
    public GetTestDetailResponse getTestDetail(String id) {
        TestEntity testEntity = getTestEntityById(id);
        GetTestDetailResponse response = mapTestEntityToResponse(testEntity);
        response.setStatus(testEntity.getStatus().name());
        List<GetQuestionsResponse.QuestionResponse> questionResponses = getQuestionResponses(id);
        String showType = testEntity.getShowResultType().toString();
        if (testEntity.getShowResultType() == null || !showType.equals(TestShowResultType.SHOW_RESULT_IMMEDIATELY.name())) {
            for (GetQuestionsResponse.QuestionResponse questionResponse : questionResponses) {
                questionResponse.setSources(fileRepository.findByOwnerIdAndOwnerType(questionResponse.getId(), FileOwnerType.QUESTION.name())
                        );
                List<GetQuestionsResponse.AnswerResponse> answerResponses = questionResponse.getAnswers();
                List<GetQuestionsResponse.AnswerResponse> newAnswerResponses = new ArrayList<>();
                for (GetQuestionsResponse.AnswerResponse answerResponse : answerResponses) {
                    answerResponse.setSource(fileRepository.findByOwnerIdAndOwnerType(answerResponse.getId(), FileOwnerType.ANSWER.name()).stream().findFirst().orElse(null));
                    newAnswerResponses.add(answerResponse.withoutIsCorrect());
                }
                questionResponse.setAnswers(newAnswerResponses);
            }
        }

        response.setQuestions(questionResponses);
        response.setTotalQuestions(questionResponses.size());
        return response;
    }

    @Override
    public GetTestDetailResponse getTestDetailForTeacher(String id, String teacherId) {
        TestEntity testEntity = getTestEntityById(id);
        if(!testEntity.getTeacherId().equals(teacherId)){
            throw new IllegalArgumentException("Teacher does not have permission to view this test");
        }
        GetTestDetailResponse response = mapTestEntityToResponse(testEntity);
        response.setStatus(testEntity.getStatus().name());
        List<GetQuestionsResponse.QuestionResponse> questionResponses = getQuestionResponses(id);

        if (testEntity.getType() == null || !testEntity.getType().equals(TestShowResultType.SHOW_RESULT_IMMEDIATELY)) {
            for (GetQuestionsResponse.QuestionResponse questionResponse : questionResponses) {
                questionResponse.setSources(fileRepository.findByOwnerIdAndOwnerType(questionResponse.getId(), FileOwnerType.QUESTION.name())
                );
                   List<GetQuestionsResponse.AnswerResponse> answerResponses = questionResponse.getAnswers();
                List<GetQuestionsResponse.AnswerResponse> newAnswerResponses = new ArrayList<>();
                for (GetQuestionsResponse.AnswerResponse answerResponse : answerResponses) {
                    answerResponse.setSource(fileRepository.findByOwnerIdAndOwnerType(answerResponse.getId(), FileOwnerType.ANSWER.name()).stream().findFirst().orElse(null));
                    answerResponse.setIsCorrect(answerRepository.findById(answerResponse.getId()).get().isCorrect());
                    newAnswerResponses.add(answerResponse);
                }

            }
        }

        response.setQuestions(questionResponses);
        response.setTotalQuestions(questionResponses.size());
        return response;
    }

    private TestEntity getTestEntityById(String id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));
    }

    private GetTestDetailResponse mapTestEntityToResponse(TestEntity testEntity) {
        GetTestDetailResponse response = new GetTestDetailResponse();
        FileEntity fileEntity = fileRepository.findByOwnerIdAndOwnerType(testEntity.getId(), FileOwnerType.TEST.name())
                .stream()
                .findFirst()
                .orElse(null);
        response.setId(testEntity.getId());
        response.setName(testEntity.getName());
        response.setDescription(testEntity.getDescription());
        response.setDuration(testEntity.getDuration());
        if (fileEntity == null) {
            response.setSource(null);
        } else {
            response.setSource(fileEntity);
        }
        response.setTeacherId(testEntity.getTeacherId());
        if (testEntity.getAttemptLimit()==null){
            response.setAttemptLimit(1);
        }
        else{

            response.setAttemptLimit(testEntity.getAttemptLimit());
        }
        response.setStartTime(Optional.ofNullable(testEntity.getStartTime()).map(Object::toString).orElse(null));
        response.setEndTime(Optional.ofNullable(testEntity.getEndTime()).map(Object::toString).orElse(null));
        response.setShowResultType(testEntity.getShowResultType().toString());
        response.setClassroomId(testEntity.getClassroomId());
        return response;
    }

    private List<GetQuestionsResponse.QuestionResponse> getQuestionResponses(String testId) {
        List<QuestionEntity> questionEntities = questionRepository.findByTestId(testId);
        return questionEntities.stream()
                .map(this::mapQuestionEntityToResponse)
                .collect(Collectors.toList());
    }

    private GetQuestionsResponse.QuestionResponse mapQuestionEntityToResponse(QuestionEntity questionEntity) {
        GetQuestionsResponse.QuestionResponse questionResponse = modelMapperService.mapClass(questionEntity, GetQuestionsResponse.QuestionResponse.class);
        questionResponse.setSources(fileRepository.findByOwnerIdAndOwnerType(questionEntity.getId(), FileOwnerType.QUESTION.name())
                );
        List<GetQuestionsResponse.AnswerResponse> answerResponses = getAnswerResponses(questionEntity.getId());
        questionResponse.setAnswers(answerResponses);
        return questionResponse;
    }

    private List<GetQuestionsResponse.AnswerResponse> getAnswerResponses(String questionId) {
        List<AnswerEntity> answerEntities = answerRepository.findByQuestionId(questionId);
        return answerEntities.stream()
                .map(this::mapAnswerEntityToResponse)
                .collect(Collectors.toList());
    }

    private GetQuestionsResponse.AnswerResponse mapAnswerEntityToResponse(AnswerEntity answerEntity) {
        GetQuestionsResponse.AnswerResponse answerResponse = modelMapperService.mapClass(answerEntity, GetQuestionsResponse.AnswerResponse.class);
        answerResponse.setIsCorrect(answerEntity.isCorrect());
        answerResponse.setSource(fileRepository.findByOwnerIdAndOwnerType(answerEntity.getId(), FileOwnerType.ANSWER.name()).stream().findFirst().orElse(null));
        return answerResponse;
    }


    @Override
    public GetTestsResponse getTestsByClassroomId(int page, int size, String classroomId, String role) {
        try{
            Pageable pageAble = PageRequest.of(page, size);
            Page<TestEntity> testEntities = null;
            if (role.equals("USER")){
                testEntities = testRepository.findByClassroomIdAndStatus(classroomId, pageAble);
            }
            else{
                 testEntities = testRepository.findByClassroomId(classroomId, pageAble);

            }
            GetTestsResponse resData = new GetTestsResponse();
            List<GetTestsResponse.TestResponse> testResponses = new ArrayList<>();
            for (TestEntity testEntity : testEntities){
                GetTestsResponse.TestResponse testResponse = modelMapperService.mapClass(testEntity, GetTestsResponse.TestResponse.class);
                testResponse.setStatus(testEntity.getStatus().name());
                testResponse.setSource(fileRepository.findByOwnerIdAndOwnerType(testEntity.getId(), FileOwnerType.TEST.name())
                        .stream()
                        .findFirst()
                        .orElse(null));
                if (testEntity.getAttemptLimit()==null){
                    testResponse.setAttemptLimit(1);
                }
                else{
                    testResponse.setAttemptLimit(testEntity.getAttemptLimit());
                }
                testResponses.add(testResponse);
            }
            resData.setTests(testResponses);
            resData.setTotalElements(testEntities.getTotalElements());
            resData.setTotalPage(testEntities.getTotalPages());
            return resData;
        }
        catch (Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public GetTestInProgress getTestInProgress(int page,int size,String studentId) {
        try{
            Pageable pageAble = PageRequest.of(page, size);
            String currentTimestamp = String.valueOf(System.currentTimeMillis());
            Slice<TestEntity> testEntities = testRepository.findTestInProgressByStudentId(studentId,currentTimestamp, pageAble);
            GetTestInProgress resData = new GetTestInProgress();
            List<GetTestInProgress.TestResponse> testResponses = new ArrayList<>();
            for (TestEntity testEntity : testEntities){
                GetTestInProgress.TestResponse testResponse = modelMapperService.mapClass(testEntity, GetTestInProgress.TestResponse.class);
                testResponse.setStatus(testEntity.getStatus().name());
                if (testEntity.getAttemptLimit()==null){
                    testResponse.setAttemptLimit(1);
                }
                else{
                    testResponse.setAttemptLimit(testEntity.getAttemptLimit());
                }
                testResponses.add(testResponse);
            }
            resData.setTests(testResponses);
            resData.setTotalElements((long) testEntities.getNumberOfElements());
            resData.setTotalPage(testEntities.getNumber());
            return resData;
        }
        catch (Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public GetTestInProgress getTestOnSpecificDayByStudentId(String studentId, String date, int page, int size) {
        try{
            Pageable pageAble = PageRequest.of(page, size);
            Slice<TestEntity> testEntities = testRepository.findTestsOnSpecificDateByStudentId(studentId, date, pageAble);
            GetTestInProgress resData = new GetTestInProgress();
            List<GetTestInProgress.TestResponse> testResponses = new ArrayList<>();
            for (TestEntity testEntity : testEntities){
                GetTestInProgress.TestResponse testResponse = modelMapperService.mapClass(testEntity, GetTestInProgress.TestResponse.class);
                testResponse.setStatus(testEntity.getStatus().name());
                testResponses.add(testResponse);
            }
            resData.setTests(testResponses);
            resData.setTotalElements((long) testEntities.getNumberOfElements());
            resData.setTotalPage(testEntities.getNumber());
            return resData;
        }
        catch (Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    @Override
    public List<TestResultResponse> getTestResult(String studentId, String testId) {
        try {
            List<TestResultEntity> testResultEntities = testResultRepository.findByStudentIdAndTestId(studentId, testId);
            if (testResultEntities.isEmpty()) {
                return new ArrayList<>();
            }
            if (testId ==null){
                return new ArrayList<>();
            }
            TestEntity testEntity = testRepository.findById(testId)
                    .orElseThrow(() -> new IllegalArgumentException("Test not found"));

            List<GetQuestionsResponse.QuestionResponse> questionResponses = getQuestionResponses(testId);

            return testResultEntities.stream().map(testResultEntity -> {
                List<StudentAnswersEntity> studentAnswersEntities = studentAnswersRepository.findByStudentIdAndTestResultId(studentId, testResultEntity.getId());

                List<GetQuestionsResponse.QuestionResponse> clonedQuestionResponses = cloneQuestionResponses(questionResponses);

                for (GetQuestionsResponse.QuestionResponse questionResponse : clonedQuestionResponses) {
                    List<GetQuestionsResponse.AnswerResponse> answerResponses = questionResponse.getAnswers();
                    for (GetQuestionsResponse.AnswerResponse answerResponse : answerResponses) {
                        StudentAnswersEntity studentAnswer = studentAnswersEntities.stream()
                                .filter(studentAnswersEntity -> studentAnswersEntity.getQuestionId().equals(questionResponse.getId()))
                                .filter(studentAnswersEntity -> studentAnswersEntity.getAnswerId().equals(answerResponse.getId()))
                                .findFirst()
                                .orElse(null);
                        if (studentAnswer != null) {
                            answerResponse.setSelected(true);
                        }
                    }
                }

                TestResultResponse resData = new TestResultResponse();
                int totalCorrect = studentAnswerRepository.countCorrectAnswersByTestResultId(testResultEntity.getId());
                int totalQuestion = questionRepository.countByTestId(testResultEntity.getTestId());
                resData.setTestId(testResultEntity.getTestId());
                resData.setGrade(testResultEntity.getGrade());
                resData.setPassed(testResultEntity.getGrade() >= 5);
                resData.setAttendedAt(testResultEntity.getAttendedAt().toString());
                resData.setCreatedAt(testResultEntity.getCreatedAt().toString());
                resData.setFinishedAt(testResultEntity.getFinishedAt().toString());
                resData.setTestType("test");
                updateSelectedAnswers(clonedQuestionResponses, studentAnswersEntities, testResultEntity.getId());
                resData.setQuestions(clonedQuestionResponses);
                resData.setTotalCorrect(totalCorrect);
                resData.setTotalIncorrect(totalQuestion - totalCorrect);
                resData.setTotalQuestions(totalQuestion);
                return resData;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public GetTestProgressResponse  getProgress(String studentId,String testId) {
        try {
            TestResultEntity testResultEntity = testResultRepository.findFirstByStudentIdAndTestIdAndStateOrderByAttendedAtDesc(studentId, testId, TestState.ONGOING.name());
            if (testResultEntity == null) {
                GetTestProgressResponse resData = new GetTestProgressResponse();
                resData.setTestResult(null);
                resData.setQuestions(new ArrayList<>());
                return resData;
            }
            TestEntity testEntity = testRepository.findById(testId).orElseThrow(() -> new IllegalArgumentException("Test does not exist"));
            if (System.currentTimeMillis() - Long.parseLong(testResultEntity.getAttendedAt()) > testEntity.getDuration()  * 1000) {
                TestSubmitRequest data= convertToTestSubmitRequest(testResultEntity,studentId);
                submitTest(data);
                GetTestProgressResponse resData = new GetTestProgressResponse();
                resData.setTestResult(null);
                resData.setQuestions(new ArrayList<>());
                return resData;
            }
            List<StudentAnswersEntity> studentAnswersEntities = studentAnswersRepository.findByStudentIdAndTestResultId(studentId, testResultEntity.getId());
            List<GetQuestionsResponse.QuestionResponse> questionResponses = getQuestionResponses(testId);
            updateSelectedAnswers(questionResponses, studentAnswersEntities, testResultEntity.getId());
            GetTestProgressResponse resData = new GetTestProgressResponse();
            resData.setTestResult(testResultEntity);
            resData.setQuestions(questionResponses);
            return resData;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    private TestSubmitRequest convertToTestSubmitRequest(TestResultEntity testResult,String studentId) {
        List<StudentAnswersEntity> studentAnswersEntities = studentAnswersRepository.findByStudentIdAndTestResultId(studentId, testResult.getId());
        List<TestSubmitRequest.QuestionAndAnswer> questionAndAnswers = new ArrayList<>();
        for (StudentAnswersEntity studentAnswer : studentAnswersEntities) {
            TestSubmitRequest.QuestionAndAnswer questionAndAnswer = questionAndAnswers.stream()
                    .filter(qa -> qa.getQuestionId().equals(studentAnswer.getQuestionId()))
                    .findFirst()
                    .orElseGet(() -> {
                        TestSubmitRequest.QuestionAndAnswer qa = new TestSubmitRequest.QuestionAndAnswer();
                        qa.setQuestionId(studentAnswer.getQuestionId());
                        qa.setAnswers(new ArrayList<>());
                        questionAndAnswers.add(qa);
                        return qa;
                    });
            questionAndAnswer.getAnswers().add(studentAnswer.getAnswerId());
        }
        TestSubmitRequest request = new TestSubmitRequest();
        request.setTestResultId(testResult.getId());
        request.setQuestionAndAnswers(questionAndAnswers);
        return request;

    }
    private List<GetQuestionsResponse.QuestionResponse> cloneQuestionResponses(List<GetQuestionsResponse.QuestionResponse> originalResponses) {
        return originalResponses.stream()
                .map(question -> {
                    GetQuestionsResponse.QuestionResponse clonedQuestion = new GetQuestionsResponse.QuestionResponse();
                    BeanUtils.copyProperties(question, clonedQuestion);

                    List<GetQuestionsResponse.AnswerResponse> clonedAnswers = question.getAnswers().stream()
                            .map(answer -> {
                                GetQuestionsResponse.AnswerResponse clonedAnswer = new GetQuestionsResponse.AnswerResponse();
                                AnswerEntity answerEntity = answerRepository.findById(answer.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
                                BeanUtils.copyProperties(answer, clonedAnswer);
                                clonedAnswer.setIsCorrect(answerEntity.isCorrect());
                                return clonedAnswer;
                            })
                            .collect(Collectors.toList());

                    clonedQuestion.setAnswers(clonedAnswers);
                    return clonedQuestion;
                })
                .collect(Collectors.toList());
    }

    private void updateSelectedAnswers(List<GetQuestionsResponse.QuestionResponse> questionResponses,
                                       List<StudentAnswersEntity> studentAnswersEntities,
                                       String testResultId) {
        for (GetQuestionsResponse.QuestionResponse questionResponse : questionResponses) {
            for (GetQuestionsResponse.AnswerResponse answerResponse : questionResponse.getAnswers()) {
                boolean isSelected = studentAnswersEntities.stream()
                        .anyMatch(studentAnswer ->
                                studentAnswer.getQuestionId().equals(questionResponse.getId()) &&
                                        studentAnswer.getAnswerId().equals(answerResponse.getId()) &&
                                        studentAnswer.getTestResultId().equals(testResultId)
                        );

                answerResponse.setSelected(isSelected);
            }
        }
    }



    @Override
    public TestSubmitResponse submitTest(TestSubmitRequest body) {
        TestResultEntity testResult = getTestResult(body.getTestResultId());
        TestEntity testEntity = testRepository.findById(testResult.getTestId())
                .orElseThrow(() -> new IllegalArgumentException("Test not found"));
        GetTestDetailResponse testDetail = getTestDetailForTeacher(testResult.getTestId(), testEntity.getTeacherId());
        List<GetQuestionsResponse.QuestionResponse> questions = testDetail.getQuestions();

        List<TestSubmitResponse.QuestionResponse> questionResponses = processQuestions(questions, body, testResult);
        int totalCorrectAnswers = calculateTotalCorrectAnswers(questionResponses);
        if (testResult.getState() == TestState.FINISHED) {
            throw  new IllegalArgumentException("Test is already finished");
        }
        updateTestResult(testResult, totalCorrectAnswers, questions.size());

        return createTestSubmitResponse(testResult, questionResponses, totalCorrectAnswers, questions.size());
    }

    private TestResultEntity getTestResult(String testResultId) {
        return testResultRepository.findById(testResultId)
                .orElseThrow(() -> new IllegalArgumentException("TestResult not found"));
    }

    private List<TestSubmitResponse.QuestionResponse> processQuestions(
            List<GetQuestionsResponse.QuestionResponse> questions,
            TestSubmitRequest body,
            TestResultEntity testResult) {
        return questions.stream()
                .map(question -> processQuestion(question, body, questions.indexOf(question), testResult))
                .collect(Collectors.toList());
    }

    private TestSubmitResponse.QuestionResponse processQuestion(
            GetQuestionsResponse.QuestionResponse question,
            TestSubmitRequest body,
            int questionIndex,
            TestResultEntity testResult) {
        TestSubmitResponse.QuestionResponse questionResponse = mapQuestionResponse(question);
        List<TestSubmitResponse.AnswerResponse> answerResponses = processAnswers(question, body, questionIndex, testResult);
        questionResponse.setAnswers(answerResponses);
        return questionResponse;
    }

    private TestSubmitResponse.QuestionResponse mapQuestionResponse(GetQuestionsResponse.QuestionResponse question) {
        TestSubmitResponse.QuestionResponse questionResponse = new TestSubmitResponse.QuestionResponse();
        questionResponse.setId(question.getId());
        questionResponse.setContent(question.getContent());
        questionResponse.setDescription(question.getDescription());
        questionResponse.setSource(fileRepository.findByOwnerIdAndOwnerType(question.getId(), FileOwnerType.QUESTION.name()));
        questionResponse.setType(question.getType());
        return questionResponse;
    }

    private List<TestSubmitResponse.AnswerResponse> processAnswers(
            GetQuestionsResponse.QuestionResponse question,
            TestSubmitRequest body,
            int questionIndex,
            TestResultEntity testResult) {
        List<String> selectedAnswers = getSelectedAnswers(body, questionIndex);
        return question.getAnswers().stream()
                .map(answer -> processAnswer(answer, selectedAnswers, testResult, question.getId()))
                .collect(Collectors.toList());
    }

    private List<String> getSelectedAnswers(TestSubmitRequest body, int questionIndex) {
        return body.getQuestionAndAnswers().size() > questionIndex
                ? body.getQuestionAndAnswers().get(questionIndex).getAnswers()
                : Collections.emptyList();
    }

    private TestSubmitResponse.AnswerResponse processAnswer(
            GetQuestionsResponse.AnswerResponse answer,
            List<String> selectedAnswers,
            TestResultEntity testResult,
            String questionId) {
        TestSubmitResponse.AnswerResponse answerResponse = mapAnswerResponse(answer);
        answerResponse.setSelected(selectedAnswers.contains(answer.getId()));

        if (answerResponse.isSelected()) {
            saveStudentAnswer(testResult, questionId, answer);
        }

        return answerResponse;
    }

    private TestSubmitResponse.AnswerResponse mapAnswerResponse(GetQuestionsResponse.AnswerResponse answer) {
        TestSubmitResponse.AnswerResponse answerResponse = new TestSubmitResponse.AnswerResponse();
        answerResponse.setId(answer.getId());
        answerResponse.setContent(answer.getContent());
        answerResponse.setCorrect(answer.getIsCorrect());
        answerResponse.setQuestionId(answer.getQuestionId());
        return answerResponse;
    }

    private void saveStudentAnswer(TestResultEntity testResult, String questionId, GetQuestionsResponse.AnswerResponse answer) {
        StudentAnswersEntity studentAnswer = studentAnswersRepository
                .findByStudentIdAndTestResultIdAndQuestionIdAndAnswerId(
                        testResult.getStudentId(), testResult.getTestId(), questionId, answer.getId());
        if (studentAnswer ==null){
            studentAnswer = new StudentAnswersEntity();
        }

        studentAnswer.setAnswerId(answer.getId());
        studentAnswer.setQuestionId(questionId);
        studentAnswer.setStudentId(testResult.getStudentId());
        studentAnswer.setTestResultId(testResult.getId());
        studentAnswer.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        studentAnswer.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
        studentAnswer.setCorrect(answer.getIsCorrect());

        studentAnswersRepository.save(studentAnswer);
    }

    private int calculateTotalCorrectAnswers(List<TestSubmitResponse.QuestionResponse> questionResponses) {
        return (int) questionResponses.stream()
                .filter(this::isQuestionCorrect)
                .count();
    }

    private boolean isQuestionCorrect(TestSubmitResponse.QuestionResponse questionResponse) {
        long correctAnswersCount = questionResponse.getAnswers().stream()
                .filter(TestSubmitResponse.AnswerResponse::isCorrect)
                .count();
        long selectedCorrectAnswersCount = questionResponse.getAnswers().stream()
                .filter(answer -> answer.isCorrect() && answer.isSelected())
                .count();
        return correctAnswersCount == selectedCorrectAnswersCount;
    }

    private void updateTestResult(TestResultEntity testResult, int totalCorrectAnswers, int totalQuestions) {
        testResult.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
        testResult.setFinishedAt(String.valueOf(System.currentTimeMillis()));
        double grade = calculateGrade(totalCorrectAnswers, totalQuestions);
        testResult.setState(TestState.FINISHED);
        testResult.setGrade(grade);
        testResultRepository.save(testResult);
    }

    private double calculateGrade(int totalCorrectAnswers, int totalQuestions) {
        double grade = (double) totalCorrectAnswers / totalQuestions * 10;
        return Math.round(grade * 100.0) / 100.0;
    }

    private TestSubmitResponse createTestSubmitResponse(
            TestResultEntity testResult,
            List<TestSubmitResponse.QuestionResponse> questionResponses,
            int totalCorrectAnswers,
            int totalQuestions) {
        TestSubmitResponse response = new TestSubmitResponse();
        response.setTestType("test");
        response.setStudentId(testResult.getStudentId());
        response.setTestId(testResult.getTestId());
        response.setAttendedAt(testResult.getAttendedAt());
        response.setTotalCorrectAnswers(totalCorrectAnswers);
        response.setTotalQuestions(totalQuestions);
        response.setFinishedAt(testResult.getFinishedAt());
        response.setAttendedAt(testResult.getAttendedAt());
        response.setGrade(testResult.getGrade());
        response.setPassed(testResult.getGrade() >= 4);
        response.setId(testResult.getId());
        return response;
    }

}
