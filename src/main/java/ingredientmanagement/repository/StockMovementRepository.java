package ingredientmanagement.repository;
import org.springframework.stereotype.Repository;
import ingredientmanagement.entity.StockValue;
import ingredientmanagement.entity.UnitEnum;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;

@Repository
public class StockMovementRepository {

    private final DataSource dataSource;

    public StockMovementRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public StockValue getStockValueAt(Integer ingredientId, Instant at, UnitEnum unit) {
        String sql = """
                SELECT unit,
                       SUM(
                           CASE
                               WHEN type = 'IN'  THEN  quantity
                               WHEN type = 'OUT' THEN -quantity
                               ELSE 0
                           END
                       ) AS actual_quantity
                FROM stock_movement
                WHERE creation_datetime <= ?
                  AND id_ingredient = ?
                  AND unit = ?::unit
                GROUP BY unit
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.from(at));
            ps.setInt(2, ingredientId);
            ps.setString(3, unit.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StockValue(
                            rs.getDouble("actual_quantity"),
                            UnitEnum.valueOf(rs.getString("unit"))
                    );
                }
                return new StockValue(0.0, unit);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}