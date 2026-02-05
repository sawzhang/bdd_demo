package com.starbucks.menu.repository;

import com.starbucks.menu.domain.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 价格历史数据访问层
 *
 * @author AI-Generated via menu-pricing skill
 * @version 1.0.0
 */
@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    /**
     * 查询产品在区域的价格历史 (按生效时间倒序)
     *
     * 对应 BDD 场景: "价格变更历史查询"
     *
     * @param productCode 产品编码
     * @param regionCode 区域代码
     * @return 价格历史列表 (按时间倒序)
     */
    List<PriceHistory> findByProductCodeAndRegionCodeOrderByEffectiveTimeDesc(
        String productCode,
        String regionCode
    );

    /**
     * 查询指定时间点的生效价格
     *
     * @param productCode 产品编码
     * @param regionCode 区域代码
     * @param queryTime 查询时间
     * @return 价格
     */
    @Query("""
        SELECT h.newPrice FROM PriceHistory h
        WHERE h.productCode = :productCode
        AND h.regionCode = :regionCode
        AND h.effectiveTime <= :queryTime
        AND (h.expiryTime IS NULL OR h.expiryTime > :queryTime)
        ORDER BY h.effectiveTime DESC
        LIMIT 1
        """)
    Optional<BigDecimal> findCurrentPrice(
        @Param("productCode") String productCode,
        @Param("regionCode") String regionCode,
        @Param("queryTime") LocalDateTime queryTime
    );
}
