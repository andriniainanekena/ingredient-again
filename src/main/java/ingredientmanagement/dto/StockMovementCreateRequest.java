package ingredientmanagement.dto;

import ingredientmanagement.entity.MovementTypeEnum;
import ingredientmanagement.entity.UnitEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementCreateRequest {
    private double value;
    private UnitEnum unit;
    private MovementTypeEnum type;
}
