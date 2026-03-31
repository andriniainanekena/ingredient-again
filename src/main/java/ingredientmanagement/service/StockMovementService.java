package ingredientmanagement.service;

import org.springframework.stereotype.Service;
import ingredientmanagement.entity.StockValue;
import ingredientmanagement.entity.UnitEnum;
import ingredientmanagement.repository.StockMovementRepository;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import ingredientmanagement.dto.StockMovementCreateRequest;
import ingredientmanagement.entity.StockMovement;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    public StockValue getStockValueAt(Integer ingredientId, Instant at, UnitEnum unit) {
        return stockMovementRepository.getStockValueAt(ingredientId, at, unit);
    }

    // f - GET /ingredients/{id}/stockMovements
    public List<StockMovement> getStockMovementsByIngredientAndDateRange(Integer ingredientId, Instant from, Instant to) {
        return stockMovementRepository.findByIngredientIdAndDateRange(ingredientId, from, to);
    }

    // f - GET /ingredients/{id}/stockMovements
    public List<StockMovement> createStockMovements(Integer ingredientId, List<StockMovementCreateRequest> requests) {
        List<StockMovement> created = new ArrayList<>();
        for (StockMovementCreateRequest req : requests) {
            StockMovement sm = stockMovementRepository.createStockMovement(
                ingredientId, req.getValue(), req.getUnit(), req.getType()
            );
            created.add(sm);
        }
        return created;
    }
}
