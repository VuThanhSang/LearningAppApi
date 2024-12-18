package com.example.learning_api.controller;

import com.example.learning_api.constant.StatusCode;
import com.example.learning_api.dto.request.test.*;
import com.example.learning_api.dto.response.question.GetQuestionsResponse;
import com.example.learning_api.dto.response.teacher.GetTeachersResponse;
import com.example.learning_api.dto.response.test.*;
import com.example.learning_api.entity.sql.database.StudentEntity;
import com.example.learning_api.entity.sql.database.UserEntity;
import com.example.learning_api.model.ResponseAPI;
import com.example.learning_api.repository.database.UserRepository;
import com.example.learning_api.service.common.JwtService;
import com.example.learning_api.service.core.ITestResultService;
import com.example.learning_api.service.core.ITestService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import static com.example.learning_api.constant.RouterConstant.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(TEST_BASE_PATH)
public class TestController {
    private final ITestService testService;
    private final ITestResultService testResultService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public ResponseEntity<ResponseAPI<String>> importTest(@ModelAttribute @Valid ImportTestRequest body) {
        try{
            testService.importTest(body);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message("Import test successfully")
                    .build();
            return new ResponseEntity<>(res, StatusCode.CREATED);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }

    }
    @GetMapping(path = "/{testId}")
    public ResponseEntity<ResponseAPI<GetTestDetailResponse>> getTestDetail(@PathVariable String testId) {
        try{
            GetTestDetailResponse resData = testService.getTestDetail(testId);
            ResponseAPI<GetTestDetailResponse> res = ResponseAPI.<GetTestDetailResponse>builder()
                    .timestamp(new Date())
                    .message("Get test detail successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<GetTestDetailResponse> res = ResponseAPI.<GetTestDetailResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

    @GetMapping(path = "/teacher/{teacherId}/{testId}")
    public ResponseEntity<ResponseAPI<GetTestDetailResponse>> getTestDetailForTeacher(@PathVariable String testId, @PathVariable String teacherId) {
        try{
            GetTestDetailResponse resData = testService.getTestDetailForTeacher(testId, teacherId);
            ResponseAPI<GetTestDetailResponse> res = ResponseAPI.<GetTestDetailResponse>builder()
                    .timestamp(new Date())
                    .message("Get test detail for teacher successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<GetTestDetailResponse> res = ResponseAPI.<GetTestDetailResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

    @PostMapping(path = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public ResponseEntity<ResponseAPI<CreateTestResponse>> createTest(@ModelAttribute @Valid CreateTestRequest body) {
        try{
            CreateTestResponse resDate = testService.createTest(body);
            ResponseAPI<CreateTestResponse> res = ResponseAPI.<CreateTestResponse>builder()
                    .timestamp(new Date())
                    .message("Create test successfully")
                    .data(resDate)
                    .build();
            return new ResponseEntity<>(res, StatusCode.CREATED);
        }
        catch (Exception e){
            ResponseAPI<CreateTestResponse> res = ResponseAPI.<CreateTestResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }

    }
    @PatchMapping(path = "/{testId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public ResponseEntity<ResponseAPI<String>> updateTest(@ModelAttribute @Valid UpdateTestRequest body, @PathVariable String testId) {
        try{
            body.setId(testId);
            testService.updateTest(body);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message("Update test successfully")
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }

    }

    @DeleteMapping(path = "/{testId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public ResponseEntity<ResponseAPI<String>> deleteTest(@PathVariable String testId) {
        try{
            testService.deleteTest(testId);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message("Delete test successfully")
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }

    }
    @GetMapping(path = "")
    public ResponseEntity<ResponseAPI<GetTestsResponse>> getTests (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search
    ){
        try{
            GetTestsResponse resData = testService.getTests(page-1, size, search);
            ResponseAPI<GetTestsResponse> res = ResponseAPI.<GetTestsResponse>builder()
                    .timestamp(new Date())
                    .message("Get tests successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<GetTestsResponse> res = ResponseAPI.<GetTestsResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }


    @PostMapping(path = "/start")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<ResponseAPI<StartTestResponse>> startTest(@RequestBody @Valid CreateTestResultRequest body) {
        try{
            StartTestResponse data = testResultService.addTestResult(body);
            ResponseAPI<StartTestResponse> res = ResponseAPI.<StartTestResponse>builder()
                    .timestamp(new Date())
                    .message("Start test successfully")
                    .data(data)
                    .build();
            return new ResponseEntity<>(res, StatusCode.CREATED);
        }
        catch (Exception e){
            ResponseAPI<StartTestResponse> res = ResponseAPI.<StartTestResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }

    }

    @PostMapping(path = "/submit")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<ResponseAPI<TestSubmitResponse>> submitTest(@RequestBody @Valid TestSubmitRequest body) {
        try{
            TestSubmitResponse data = testService.submitTest(body);
            ResponseAPI<TestSubmitResponse> res = ResponseAPI.<TestSubmitResponse>builder()
                    .timestamp(new Date())
                    .message("Submit test successfully")
                    .data(data)
                    .build();
            return new ResponseEntity<>(res, StatusCode.CREATED);
        }
        catch (Exception e){
            ResponseAPI<TestSubmitResponse> res = ResponseAPI.<TestSubmitResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }

    }

    @GetMapping(path = "/classroom/{classroomId}")
    @PreAuthorize("hasAnyAuthority('USER','TEACHER','ADMIN')")
    public ResponseEntity<ResponseAPI<GetTestsResponse>> getTestsByClassroomId (
            @PathVariable String classroomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ){
        try{
            String token = request.getHeader("Authorization");
            // If the token is prefixed with "Bearer ", you may need to remove it
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String studentId = jwtService.extractUserId(token);
            UserEntity user = userRepository.findById(studentId).orElseThrow(() -> new Exception("User not found"));

            GetTestsResponse resData = testService.getTestsByClassroomId(page-1, size, classroomId, user.getRole().name());
            ResponseAPI<GetTestsResponse> res = ResponseAPI.<GetTestsResponse>builder()
                    .timestamp(new Date())
                    .message("Get tests by classroom id successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<GetTestsResponse> res = ResponseAPI.<GetTestsResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

    @PostMapping(path = "/result")
    public ResponseEntity<ResponseAPI<String>> addTestResult(@RequestBody @Valid CreateTestResultRequest body) {
        try{
            testResultService.addTestResult(body);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message("Add test result successfully")
                    .build();
            return new ResponseEntity<>(res, StatusCode.CREATED);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }

    }

    @PostMapping(path = "/save-progress")
    public ResponseEntity<ResponseAPI<String>> saveProgress(@RequestBody @Valid SaveProgressRequest body) {
        try{
            testResultService.saveProgress(body);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message("Save progress successfully")
                    .build();
            return new ResponseEntity<>(res, StatusCode.CREATED);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }

    }

    @GetMapping(path = "/progress/{studentId}/{testId}")
    public ResponseEntity<ResponseAPI<GetTestProgressResponse>> getProgress(@PathVariable String studentId, @PathVariable String testId) {
        try{
            GetTestProgressResponse resData = testService.getProgress(studentId, testId);
            ResponseAPI<GetTestProgressResponse> res = ResponseAPI.<GetTestProgressResponse>builder()
                    .timestamp(new Date())
                    .message("Get progress successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<GetTestProgressResponse> res = ResponseAPI.<GetTestProgressResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

    @PatchMapping(path = "/result/{testResultId}")
    public ResponseEntity<ResponseAPI<String>> updateTestResult(@RequestBody @Valid UpdateTestResultRequest body, @PathVariable String testResultId) {
        try{
            body.setId(testResultId);
            testResultService.updateTestResult(body);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message("Update test result successfully")
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }

    }

    @DeleteMapping(path = "/result/{studentId}/{courseId}")
    public ResponseEntity<ResponseAPI<String>> deleteTestResult(@PathVariable String studentId, @PathVariable String courseId) {
        try{
            testResultService.deleteTestResult(studentId, courseId);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message("Delete test result successfully")
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }

    }

    @GetMapping(path = "/result/{studentId}/{testId}")
    public ResponseEntity<ResponseAPI<List<TestResultResponse>>> getTestResult(@PathVariable String studentId, @PathVariable String testId) {
        try{
            List<TestResultResponse> resData = testService.getTestResult(studentId, testId);
            ResponseAPI<List<TestResultResponse>> res = ResponseAPI.<List<TestResultResponse>>builder()
                    .timestamp(new Date())
                    .message("Get test result successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<List<TestResultResponse>> res = ResponseAPI.<List<TestResultResponse>>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }
    @GetMapping(path = "/result/classroom/{classroomId}")
    public ResponseEntity<ResponseAPI<List<TestResultsForClassroomResponse>>> getTestResultsForClassroom(@PathVariable String classroomId) {
        try{
            List<TestResultsForClassroomResponse> resData = testResultService.getTestResultsForClassroom(classroomId);
            ResponseAPI<List<TestResultsForClassroomResponse>> res = ResponseAPI.<List<TestResultsForClassroomResponse>>builder()
                    .timestamp(new Date())
                    .message("Get test results for classroom successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<List<TestResultsForClassroomResponse>> res = ResponseAPI.<List<TestResultsForClassroomResponse>>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

    @GetMapping(path = "/result/student/{studentId}/classroom/{classroomId}")
    public ResponseEntity<ResponseAPI<List<TestResultForStudentResponse>>> getTestResultsByStudentIdAndClassroomId(@PathVariable String studentId, @PathVariable String classroomId) {
        try{
            List<TestResultForStudentResponse> resData = testResultService.getTestResultsByStudentIdAndClassroomId(studentId, classroomId);
            ResponseAPI<List<TestResultForStudentResponse>> res = ResponseAPI.<List<TestResultForStudentResponse>>builder()
                    .timestamp(new Date())
                    .message("Get test results by student id and classroom id successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<List<TestResultForStudentResponse>> res = ResponseAPI.<List<TestResultForStudentResponse>>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

    @GetMapping(path = "/result/overview/{testId}")
    public ResponseEntity<ResponseAPI<OverviewResultResponse>> getOverviewOfTestResults(@PathVariable String testId) {
        try{
            OverviewResultResponse resData = testResultService.getOverviewOfTestResults(testId);
            ResponseAPI<OverviewResultResponse> res = ResponseAPI.<OverviewResultResponse>builder()
                    .timestamp(new Date())
                    .message("Get overview of test results successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<OverviewResultResponse> res = ResponseAPI.<OverviewResultResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

    @GetMapping(path = "/result/statistics/{testId}")
    public ResponseEntity<ResponseAPI<StatisticsResultResponse>> getStatisticsQuestionAndAnswerOfTest(@PathVariable String testId) {
        try{
            StatisticsResultResponse resData = testResultService.getStatisticsQuestionAndAnswerOfTest(testId);
            ResponseAPI<StatisticsResultResponse> res = ResponseAPI.<StatisticsResultResponse>builder()
                    .timestamp(new Date())
                    .message("Get statistics question and answer of test successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<StatisticsResultResponse> res = ResponseAPI.<StatisticsResultResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

    @GetMapping(path = "/result/student/not-attempted/{testId}")
    public ResponseEntity<ResponseAPI<List<StudentEntity>>> getStudentNotAttemptedTest(@PathVariable String testId) {
        try{
            List<StudentEntity> resData = testResultService.getStudentNotAttemptedTest(testId);
            ResponseAPI<List<StudentEntity>> res = ResponseAPI.<List<StudentEntity>>builder()
                    .timestamp(new Date())
                    .message("Get student not attempted test successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        }
        catch (Exception e){
            ResponseAPI<List<StudentEntity>> res = ResponseAPI.<List<StudentEntity>>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

    @GetMapping(path = "/result/score-distribution/{testId}")
    public ResponseEntity<ResponseAPI<ScoreDistributionResponse>> getScoreDistributionOfTest(
            @PathVariable String testId,
            @RequestParam(required = false) String fullname,
            @RequestParam(required = false) Integer minGrade,
            @RequestParam(required = false) Integer maxGrade,
            @RequestParam(required = false) Boolean passed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "grade") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        try {
            if (fullname == null) fullname = "";
            ScoreDistributionResponse resData = testResultService.getScoreDistributionOfTest(testId, fullname, minGrade, maxGrade, passed, page-1, size, sortBy, sortOrder);
            ResponseAPI<ScoreDistributionResponse> res = ResponseAPI.<ScoreDistributionResponse>builder()
                    .timestamp(new Date())
                    .message("Get score distribution of test successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        } catch (Exception e) {
            ResponseAPI<ScoreDistributionResponse> res = ResponseAPI.<ScoreDistributionResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

    @GetMapping(path = "/result/question-choice-rate/{testId}")
    public ResponseEntity<ResponseAPI<GetQuestionChoiceRateResponse>> getQuestionChoiceRate(
            @PathVariable String testId,
            @RequestParam(required = false) String questionContent,
            @RequestParam(required = false) Integer minCorrectCount,
            @RequestParam(required = false) Integer maxCorrectCount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "totalCorrect") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        try {
            if (questionContent == null) questionContent = "";
            GetQuestionChoiceRateResponse resData = testResultService.getQuestionChoiceRate(testId, questionContent, minCorrectCount, maxCorrectCount, page-1, size, sortBy, sortOrder);
            ResponseAPI<GetQuestionChoiceRateResponse> res = ResponseAPI.<GetQuestionChoiceRateResponse>builder()
                    .timestamp(new Date())
                    .message("Get question choice rate successfully")
                    .data(resData)
                    .build();
            return new ResponseEntity<>(res, StatusCode.OK);
        } catch (Exception e) {
            ResponseAPI<GetQuestionChoiceRateResponse> res = ResponseAPI.<GetQuestionChoiceRateResponse>builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(res, StatusCode.BAD_REQUEST);
        }
    }

}
