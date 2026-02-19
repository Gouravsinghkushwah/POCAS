package com.pocas.repo;

import com.pocas.entity.CollectionAccount;
import com.pocas.entity.DailyCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DailyCollectionRepository extends JpaRepository<DailyCollection, Long> {

    List<DailyCollection> findByCollectionAccount(CollectionAccount collectionAccount);

    List<DailyCollection> findByCollectionAccountAndMonthAndYear(CollectionAccount collectionAccount, Integer month, Integer year);

    List<DailyCollection> findByCollectionDateBetween(LocalDate start, LocalDate end);

    @Query(value = "SELECT SUM(d.collected_amount) " +
            "FROM daily_collections d " +
            "WHERE d.account_id = :accountId " +
            "AND d.month = :month " +
            "AND d.year = :year", nativeQuery = true)
    BigDecimal sumMonthlyCollection(@Param("accountId") Long accountId,
                               @Param("month") Integer month,
                               @Param("year") Integer year);
}