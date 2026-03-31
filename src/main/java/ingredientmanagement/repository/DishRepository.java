package ingredientmanagement.repository;

import org.springframework.stereotype.Repository;
import school.hei.ingredientspringboot.entity.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishRepository {

    private final DataSource dataSource;
    private final IngredientRepository ingredientRepository;

    public DishRepository(DataSource dataSource, IngredientRepository ingredientRepository) {
        this.dataSource = dataSource;
        this.ingredientRepository = ingredientRepository;
    }

    public List<Dish> getDishes() {
        String sql = "SELECT id, name, selling_price, dish_type FROM dish ORDER BY id ASC";
        List<Dish> dishes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                Dish dish = new Dish(
                        id,
                        rs.getString("name"),
                        rs.getObject("selling_price") == null ? null : rs.getDouble("selling_price"),
                        DishTypeEnum.valueOf(rs.getString("dish_type")),
                        getIngredientsByDishId(id)
                );
                dishes.add(dish);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dishes;
    }

    public Dish getDishById(Integer id) {
        String sql = "SELECT id, name, selling_price, dish_type FROM dish WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Dish(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getObject("selling_price") == null ? null : rs.getDouble("selling_price"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            getIngredientsByDishId(id)
                    );
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DishIngredient> getIngredientsByDishId(Integer dishId) {
        String sql = """
                SELECT i.id, i.name, i.price, i.category, di.required_quantity, di.unit
                FROM ingredient i
                JOIN dish_ingredient di ON di.id_ingredient = i.id
                WHERE di.id_dish = ?
                """;
        List<DishIngredient> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                // Dans DishRepository.java -> getIngredientsByDishId
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            CategoryEnum.valueOf(rs.getString("category")),
                            rs.getDouble("price")
                    );

                    // Sécurisation de l'unité (UnitEnum)
                    String unitStr = rs.getString("unit");
                    UnitEnum unit = (unitStr == null) ? null : UnitEnum.valueOf(unitStr);

                    // Sécurisation de la quantité
                    Double quantity = rs.getObject("required_quantity") == null ? null : rs.getDouble("required_quantity");

                    DishIngredient di = new DishIngredient(
                            ingredient,
                            quantity,
                            unit
                    );
                    list.add(di);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public Dish updateDishIngredients(Integer dishId, List<Ingredient> requestedIngredients) {
        List<Integer> validIngredientIds = new ArrayList<>();
        for (Ingredient req : requestedIngredients) {
            if (req.getId() != null) {
                Ingredient fromDb = ingredientRepository.getIngredientById(req.getId());
                if (fromDb != null) {
                    validIngredientIds.add(fromDb.getId());
                }
            }
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM dish_ingredient WHERE id_dish = ?")) {
                del.setInt(1, dishId);
                del.executeUpdate();
            }

            String insertSql = "INSERT INTO dish_ingredient (id_dish, id_ingredient) VALUES (?, ?)";
            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                for (Integer ingId : validIngredientIds) {
                    ins.setInt(1, dishId);
                    ins.setInt(2, ingId);
                    ins.addBatch();
                }
                ins.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return getDishById(dishId);
    }
  }
