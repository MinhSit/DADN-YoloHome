package yolohome.backend.repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import yolohome.backend.entity.SensorReading;

/**
 * Xay dung dieu kien WHERE dong cho SensorReading, chi them predicate khi tham so
 * duoc truyen vao (khac null). Cach nay tranh loi "could not determine data type
 * of parameter" cua PostgreSQL JDBC driver - loi xay ra khi dung JPQL kieu
 * "(:param is null or ...)" ma tham so do luon null, khien driver khong the
 * suy luan kieu du lieu cho parameter binding.
 */
public class SensorReadingSpecifications {

    private SensorReadingSpecifications() {
    }

    public static Specification<SensorReading> withFilters(
            String deviceId, OffsetDateTime from, OffsetDateTime to) {

        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (deviceId != null && !deviceId.isBlank()) {
                predicates.add(cb.equal(root.get("deviceId"), deviceId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("recordedAt"), to));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
