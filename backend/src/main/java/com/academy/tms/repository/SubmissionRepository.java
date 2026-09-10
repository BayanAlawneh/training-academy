package com.academy.tms.repository;

import com.academy.tms.entities.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByExamIdAndTraineeId(Long examId, Long traineeId);

    boolean existsByExamIdAndTraineeId(Long examId, Long traineeId);

    @Query("""
           select s from Submission s
           join fetch s.trainee t
           join fetch t.user
           where s.exam.id = :examId
           order by s.score desc
           """)
    List<Submission> findAllByExamIdWithTrainee(@Param("examId") Long examId);

    @Query("""
           select s from Submission s
           join fetch s.exam e
           join fetch e.course
           where s.trainee.id = :traineeId
           order by s.submittedAt desc
           """)
    List<Submission> findAllByTraineeId(@Param("traineeId") Long traineeId);

    /**
     * أزواج المطابقة تشير إلى الإجابات، فتُحذف قبلها.
     * تركها يجعل حذف المتدرّب يخرق قيد المفتاح الأجنبي.
     */
    @Modifying
    @Query("""
           delete from AnswerPair p where p.answer.id in (
               select a.id from Answer a where a.submission.id in (
                   select s.id from Submission s where s.trainee.id = :traineeId))
           """)
    int deleteAnswerPairsByTraineeId(@Param("traineeId") Long traineeId);

    @Modifying
    @Query("delete from Answer a where a.submission.id in (select s.id from Submission s where s.trainee.id = :traineeId)")
    int deleteAnswersByTraineeId(@Param("traineeId") Long traineeId);

    @Modifying
    @Query("delete from Submission s where s.trainee.id = :traineeId")
    int deleteAllByTraineeId(@Param("traineeId") Long traineeId);
}
