package com.diary.repository;

import com.diary.model.DiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    /** q = "" and category = "" mean "no filter". pattern is "%text%" in lower case. */
    @Query("""
        SELECT e FROM DiaryEntry e
        WHERE e.owner.id = :ownerId
          AND e.entryDate BETWEEN :fromDate AND :toDate
          AND (:category = '' OR e.category = :category)
          AND (:q = '' OR LOWER(e.title) LIKE :pattern OR LOWER(e.content) LIKE :pattern)
        ORDER BY e.entryDate DESC, e.id DESC
        """)
    List<DiaryEntry> search(@Param("ownerId") Long ownerId,
                            @Param("q") String q,
                            @Param("pattern") String pattern,
                            @Param("category") String category,
                            @Param("fromDate") LocalDate fromDate,
                            @Param("toDate") LocalDate toDate);

    Optional<DiaryEntry> findByIdAndOwnerId(Long id, Long ownerId);

    List<DiaryEntry> findByOwnerIdAndEntryDateBetween(Long ownerId, LocalDate from, LocalDate to);
}
