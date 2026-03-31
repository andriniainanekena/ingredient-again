package ingredientmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DishIngredient {
    private Ingredient ingredient;
    private Double quantity;
    private UnitEnum unit;
}
