package ingredientmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ingredientmanagement.entity.Ingredient;
import ingredientmanagement.entity.StockValue;
import ingredientmanagement.entity.UnitEnum;
import ingredientmanagement.service.IngredientService;

import java.time.Instant;
import java.util.List;
import ingredientmanagement.dto.StockMovementCreateRequest;
import ingredientmanagement.entity.StockMovement;
import ingredientmanagement.service.StockMovementService;

@RestController
public class IngredientController {

    private final IngredientService ingredientService;
    private final StockMovementService stockMovementService;

    public IngredientController(IngredientService ingredientService, StockMovementService stockMovementService) {
        this.ingredientService = ingredientService;
        this.stockMovementService = stockMovementService;
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<Ingredient>> getIngredients() {
        return ResponseEntity.ok(ingredientService.getIngredients());
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable Integer id) {
        Ingredient ingredient = ingredientService.getIngredientById(id);
        if (ingredient == null) {
            return ResponseEntity.status(404)
                    .body("Ingredient.id=" + id + " is not found");
        }
        return ResponseEntity.ok(ingredient);
    }

    @GetMapping("/ingredients/{id}/stock")
    public ResponseEntity<?> getIngredientStock(
            @PathVariable Integer id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit) {

        if (at == null || unit == null) {
            return ResponseEntity.status(400)
                    .body("Either mandatory query parameter `at` or `unit` is not provided.");
        }

        Ingredient ingredient = ingredientService.getIngredientById(id);
        if (ingredient == null) {
            return ResponseEntity.status(404)
                    .body("Ingredient.id=" + id + " is not found");
        }

        Instant atInstant;
        UnitEnum unitEnum;
        try {
            atInstant = Instant.parse(at);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body("Invalid value for `at` parameter. Expected ISO-8601 format (e.g. 2024-01-06T12:00:00Z).");
        }
        try {
            unitEnum = UnitEnum.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body("Invalid value for `unit` parameter. Expected one of: PCS, KG, L.");
        }

        StockValue stockValue = ingredientService.getStockAt(id, atInstant, unitEnum);
        return ResponseEntity.ok(stockValue);
    }
    // f - GET /ingredients/{id}/stockMovements
    @GetMapping("/ingredients/{id}/stockMovements")
    public ResponseEntity<?> getStockMovements(
            @PathVariable Integer id,
            @RequestParam String from,
            @RequestParam String to) {

        Ingredient ingredient = ingredientService.getIngredientById(id);
        if (ingredient == null) {
            return ResponseEntity.status(404)
                    .body("Ingredient.id=" + id + " is not found");
        }

        Instant fromInstant;
        Instant toInstant;
        try {
            fromInstant = Instant.parse(from);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body("Invalid value for `from` parameter. Expected ISO-8601 format (e.g. 2024-01-06T12:00:00Z).");
        }
        try {
            toInstant = Instant.parse(to);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body("Invalid value for `to` parameter. Expected ISO-8601 format (e.g. 2024-01-06T12:00:00Z).");
        }
        if (fromInstant.isAfter(toInstant)) {
            return ResponseEntity.status(400)
                    .body("`from` must be before or equal to `to`.");
        }

        List<StockMovement> movements = stockMovementService.getStockMovementsByIngredientAndDateRange(id, fromInstant, toInstant);
        return ResponseEntity.ok(movements);
    }

    @PostMapping("/ingredients/{id}/stockMovements")
    public ResponseEntity<?> addStockMovements(
            @PathVariable Integer id,
            @RequestBody List<StockMovementCreateRequest> requests) {

        Ingredient ingredient = ingredientService.getIngredientById(id);
        if (ingredient == null) {
            return ResponseEntity.status(404)
                    .body("Ingredient.id=" + id + " is not found");
        }

        List<StockMovement> created = stockMovementService.createStockMovements(id, requests);
        return ResponseEntity.ok(created);
    }
}
