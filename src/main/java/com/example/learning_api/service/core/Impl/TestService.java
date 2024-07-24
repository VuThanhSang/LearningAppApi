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
import com.example.learning_api.enums.ImportType;
import com.example.learning_api.enums.TestShowResultType;
import com.example.learning_api.enums.TestStatus;
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

    @Override
    public CreateTestResponse createTest(CreateTestRequest body) {
        try{
            TeacherEntity userEntity = teacherRepository.findById(body.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

            if (body.getTeacherId()==null){
                throw new IllegalArgumentException("TeacherID is required");
            }
            if (userEntity==null){
                throw new IllegalArgumentException("TeacherID is not found");
            }
            if (body.getClassroomId() == null){
                throw new IllegalArgumentException("ClassroomId is required");
            }
            if (classRoomRepository.findById(body.getClassroomId()).isEmpty()){
                throw new IllegalArgumentException("ClassroomId is not found");
            }
            CreateTestResponse resData = new CreateTestResponse();
            TestEntity testEntity = modelMapperService.mapClass(body, TestEntity.class);
            if(body.getSource()!=null){
                if (!ImageUtils.isValidImageFile(body.getSource()) && body.getSource()!=null) {
                    throw new CustomException(ErrorConstant.IMAGE_INVALID);
                }
                byte[] originalImage = new byte[0];
                originalImage = body.getSource().getBytes();
                byte[] newImage = ImageUtils.resizeImage(originalImage, 200, 200);
                CloudinaryUploadResponse imageUploaded = cloudinaryService.uploadFileToFolder(
                        CloudinaryConstant.CLASSROOM_PATH,
                        StringUtils.generateFileName(body.getName(), "tests"),
                        newImage,
                        "image"
                );
                testEntity.setSource(imageUploaded.getUrl());
            }
            testEntity.setCreatedAt(new Date());

            testEntity.setUpdatedAt(new Date());
            testRepository.save(testEntity);
            resData.setTeacherId(body.getTeacherId());
            resData.setCreatedAt(testEntity.getCreatedAt().toString());
            resData.setDescription(body.getDescription());
            resData.setDuration(body.getDuration());
            resData.setId(testEntity.getId());
            resData.setSource(testEntity.getSource());
            resData.setName(body.getName());
            resData.setUpdatedAt(testEntity.getUpdatedAt().toString());
            resData.setStartTime(testEntity.getStartTime());
            resData.setEndTime(testEntity.getEndTime());
            resData.setClassroomId(body.getClassroomId());
            resData.setShowResultType(body.getShowResultType());
            resData.setStatus(body.getStatus());
            resData.setAttemptLimit(body.getAttemptLimit());
            return resData;

        }
        catch (Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }
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
            testEntity.setUpdatedAt(new Date());
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
                byte[] originalImage = new byte[0];
                originalImage = body.getSource().getBytes();
                byte[] newImage = ImageUtils.resizeImage(originalImage, 200, 200);
                CloudinaryUploadResponse imageUploaded = cloudinaryService.uploadFileToFolder(
                        CloudinaryConstant.CLASSROOM_PATH,
                        StringUtils.generateFileName(body.getName(), "tests"),
                        newImage,
                        "image"
                );
                testEntity.setSource(imageUploaded.getUrl());
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
            if (body.getAttemptLimit()!=0){
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
            cloudinaryService.deleteImage(testEntity.getSource());
            List<QuestionEntity> questionEntities = questionRepository.findByTestId(id);
            for (QuestionEntity questionEntity : questionEntities){
                answerRepository.deleteByQuestionId(questionEntity.getId());
            }
            questionRepository.deleteByTestId(id);
            testRepository.deleteById(id);
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
        question.setSource("");
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
        answer.setSource("");
        answer.setCreatedAt(new Date());
        answer.setUpdatedAt(new Date());
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
        List<GetQuestionsResponse.QuestionResponse> questionResponses = getQuestionResponses(id);
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
        response.setId(testEntity.getId());
        response.setName(testEntity.getName());
        response.setDescription(testEntity.getDescription());
        response.setDuration(testEntity.getDuration());
        response.setSource(testEntity.getSource());
        response.setTeacherId(testEntity.getTeacherId());
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
        List<GetQuestionsResponse.AnswerResponse> answerResponses = getAnswerResponses(questionEntity.getId());
        questionResponse.setAnswers(answerResponses);
        return questionResponse;
    }

    private List<GetQuestionsResponse.AnswerResponse> getAnswerResponses(String questionId) {
        List<AnswerEntity> answerEntities = answerRepository.findByQuestionId(questionId);
        return answerEntities.stream()
                .map(answerEntity -> modelMapperService.mapClass(answerEntity, GetQuestionsResponse.AnswerResponse.class))
                .collect(Collectors.toList());
    }




    @Override
    public GetTestsResponse getTestsByClassroomId(int page, int size, String classroomId) {
        try{
            Pageable pageAble = PageRequest.of(page, size);
            Page<TestEntity> testEntities = testRepository.findByClassroomId(classroomId, pageAble);
            GetTestsResponse resData = new GetTestsResponse();
            List<GetTestsResponse.TestResponse> testResponses = new ArrayList<>();
            for (TestEntity testEntity : testEntities){
                GetTestsResponse.TestResponse testResponse = modelMapperService.mapClass(testEntity, GetTestsResponse.TestResponse.class);
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
            String currentTimestamp = String.valueOf(System.currentTimeMillis() );
            Slice<TestEntity> testEntities = testRepository.findTestInProgressByStudentId(studentId,currentTimestamp, pageAble);
            GetTestInProgress resData = new GetTestInProgress();
            List<GetTestInProgress.TestResponse> testResponses = new ArrayList<>();
            for (TestEntity testEntity : testEntities){
                GetTestInProgress.TestResponse testResponse = modelMapperService.mapClass(testEntity, GetTestInProgress.TestResponse.class);
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
                throw new IllegalArgumentException("TestResult not found");
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
                resData.setTestId(testResultEntity.getTestId());
                resData.setGrade(testResultEntity.getGrade());
                resData.setPassed(testResultEntity.getGrade() >= 5);
                resData.setAttendedAt(testResultEntity.getAttendedAt().toString());
                resData.setCreatedAt(testResultEntity.getCreatedAt().toString());
                resData.setTestType("test");
                updateSelectedAnswers(clonedQuestionResponses, studentAnswersEntities, testResultEntity.getId());
                resData.setQuestions(clonedQuestionResponses);

                return resData;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private List<GetQuestionsResponse.QuestionResponse> cloneQuestionResponses(List<GetQuestionsResponse.QuestionResponse> originalResponses) {
        return originalResponses.stream()
                .map(question -> {
                    GetQuestionsResponse.QuestionResponse clonedQuestion = new GetQuestionsResponse.QuestionResponse();
                    BeanUtils.copyProperties(question, clonedQuestion);

                    List<GetQuestionsResponse.AnswerResponse> clonedAnswers = question.getAnswers().stream()
                            .map(answer -> {
                                GetQuestionsResponse.AnswerResponse clonedAnswer = new GetQuestionsResponse.AnswerResponse();
                                BeanUtils.copyProperties(answer, clonedAnswer);
                                clonedAnswer.setSelected(false);  // Ensure all answers start as unselected
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
        GetTestDetailResponse testDetail = getTestDetail(testResult.getTestId());
        List<GetQuestionsResponse.QuestionResponse> questions = testDetail.getQuestions();

        List<TestSubmitResponse.QuestionResponse> questionResponses = processQuestions(questions, body, testResult);
        int totalCorrectAnswers = calculateTotalCorrectAnswers(questionResponses);

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
        questionResponse.setSource(question.getSource());
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
        answerResponse.setCorrect(answer.isCorrect());
        answerResponse.setSource(answer.getSource());
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
        studentAnswer.setCorrect(answer.isCorrect());

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
        testResult.setAttendedAt(String.valueOf(System.currentTimeMillis()));
        testResult.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        testResult.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
        double grade = calculateGrade(totalCorrectAnswers, totalQuestions);
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
//        response.setQuestions(questionResponses);
        response.setGrade(testResult.getGrade());
        response.setPassed(testResult.getGrade() >= 4);
        return response;
    }

}
