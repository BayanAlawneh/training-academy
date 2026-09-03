package com.academy.tms.repository;

import com.academy.tms.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
           select distinct q from Question q
           left join fetch q.options
           where q.exam.id = :examId
           order by q.position
           """)
    List<Question> findAllByExamIdWithOptions(@Param("examId") Long examId);

    long countByExamId(Long examId);
}
