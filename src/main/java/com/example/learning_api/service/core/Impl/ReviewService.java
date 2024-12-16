package com.example.learning_api.service.core.Impl;

import com.example.learning_api.entity.sql.database.ClassRoomEntity;
import com.example.learning_api.entity.sql.database.ReviewEntity;
import com.example.learning_api.repository.database.ClassRoomRepository;
import com.example.learning_api.repository.database.ReviewRepository;
import com.example.learning_api.service.core.IReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final ClassRoomRepository classRoomRepository;
    @Override
    public void createReview(ReviewEntity review) {
        try{
            ReviewEntity reviewEntity = new ReviewEntity();
            reviewEntity.setRating(review.getRating());
            reviewEntity.setContent(review.getContent());
            reviewEntity.setTitle(review.getTitle());
            reviewEntity.setUserId(review.getUserId());
            ClassRoomEntity classRoomEntity = classRoomRepository.findById(review.getClassroomId()).orElse(null);
            if (classRoomEntity == null){
                throw new IllegalArgumentException("Classroom not found");
            }
            reviewEntity.setClassroomId(review.getClassroomId());
            reviewEntity.setCreatedAt(String.valueOf(System.currentTimeMillis()));
            reviewEntity.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
            reviewRepository.save(reviewEntity);
        }
        catch (Exception e){
            log.error("Error in creating review: ", e);
        }
    }

    @Override
    public void updateReview(String id, ReviewEntity review) {
        try{
            ReviewEntity reviewEntity = reviewRepository.findById(id).orElse(null);
            if (reviewEntity == null){
                throw new IllegalArgumentException("Review not found");
            }
            if (review.getRating() != null)
                reviewEntity.setRating(review.getRating());
            if (review.getContent() != null)
                reviewEntity.setContent(review.getContent());
            reviewEntity.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
            reviewRepository.save(reviewEntity);
        }
        catch (Exception e){
            log.error("Error in updating review: ", e);
        }

    }

    @Override
    public void deleteReview(String id) {
        try{
            ReviewEntity reviewEntity = reviewRepository.findById(id).orElse(null);
            if (reviewEntity == null){
                throw new IllegalArgumentException("Review not found");
            }
            reviewRepository.deleteById(id);
        }
        catch (Exception e){
            log.error("Error in deleting review: ", e);
        }
    }
}