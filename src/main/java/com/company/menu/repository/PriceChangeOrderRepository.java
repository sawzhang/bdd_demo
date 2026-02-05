package com.company.menu.repository;

import com.company.menu.domain.PriceChangeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 价格变更单数据访问层
 *
 * @author AI-Generated via menu-pricing skill
 * @version 1.0.0
 */
@Repository
public interface PriceChangeOrderRepository extends JpaRepository<PriceChangeOrder, Long> {

    /**
     * 根据变更单号查询
     *
     * @param orderNo 变更单号
     * @return 价格变更单
     */
    Optional<PriceChangeOrder> findByOrderNo(String orderNo);

    /**
     * 统计指定日期创建的变更单数量 (用于生成序号)
     *
     * @param date 日期
     * @return 数量
     */
    @Query("SELECT COUNT(o) FROM PriceChangeOrder o WHERE CAST(o.createdAt AS LocalDate) = :date")
    int countByCreatedAtDate(@Param("date") LocalDate date);
}
