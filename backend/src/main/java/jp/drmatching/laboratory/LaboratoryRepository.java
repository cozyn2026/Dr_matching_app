package jp.drmatching.laboratory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {
    java.util.List<Laboratory> findAllByUniversityNameIgnoreCaseOrderByIdAsc(String universityName);
    java.util.Optional<Laboratory> findByIdAndUniversityNameIgnoreCase(Long id, String universityName);

    @Query("""
            select l from Laboratory l
            where lower(l.universityName) like lower(concat('%', :keyword, '%')) escape '!'
               or lower(l.graduateSchoolName) like lower(concat('%', :keyword, '%')) escape '!'
               or lower(l.laboratoryName) like lower(concat('%', :keyword, '%')) escape '!'
               or lower(l.supervisorName) like lower(concat('%', :keyword, '%')) escape '!'
               or lower(l.researchField) like lower(concat('%', :keyword, '%')) escape '!'
               or lower(l.researchSummary) like lower(concat('%', :keyword, '%')) escape '!'
            order by l.id
            """)
    java.util.List<Laboratory> search(@Param("keyword") String keyword);
}
