package ingredientmanagement.repository;
import org.springframework.stereotype.Repository;
import ingredientmanagement.entity.StockValue;
import ingredientmanagement.entity.UnitEnum;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import ingredientmanagement.entity.StockMovement;
import ingredientmanagement.entity.MovementTypeEnum;

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

    // f - GET /ingredients/{id}/stockMovements
    public List<StockMovement> findByIngredientIdAndDateRange(Integer ingredientId, Instant from, Instant to) {
        String sql = """
                SELECT id, quantity, unit, type, creation_datetime
                FROM stock_movement
                WHERE id_ingredient = ? AND creation_datetime >= ? AND creation_datetime <= ?
                ORDER BY creation_datetime ASC
                """;

        List<StockMovement> movements = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ingredientId);
            ps.setTimestamp(2, Timestamp.from(from));
            ps.setTimestamp(3, Timestamp.from(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movements.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return movements;
    }
    // f - GET /ingredients/{id}/stockMovements
    public StockMovement createStockMovement(int ingredientId, double quantity, UnitEnum unit, MovementTypeEnum type) {
        String sql = """
                INSERT INTO stock_movement (id_ingredient, quantity, unit, type, creation_datetime)
                VALUES (?, ?, ?::unit, ?, NOW() AT TIME ZONE 'UTC')
                RETURNING id, quantity, unit, type, creation_datetime
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ingredientId);
            ps.setDouble(2, quantity);
            ps.setString(3, unit.name());
            ps.setString(4, type.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                throw new RuntimeException("Failed to create stock movement");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private StockMovement mapRow(ResultSet rs) throws SQLException {
        return new StockMovement(
            rs.getInt("id"),
            new StockValue(
                rs.getDouble("quantity"),
                UnitEnum.valueOf(rs.getString("unit"))
            ),
            MovementTypeEnum.valueOf(rs.getString("type")),
            rs.getTimestamp("creation_datetime").toInstant()
        );
    }
}
