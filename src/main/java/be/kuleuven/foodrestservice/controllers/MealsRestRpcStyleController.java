package be.kuleuven.foodrestservice.controllers;

import be.kuleuven.foodrestservice.domain.Meal;
import be.kuleuven.foodrestservice.domain.MealsRepository;
import be.kuleuven.foodrestservice.exceptions.MealNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
public class MealsRestRpcStyleController {

    private final MealsRepository mealsRepository;

    @Autowired
    MealsRestRpcStyleController(MealsRepository mealsRepository) {
        this.mealsRepository = mealsRepository;
    }

    @GetMapping("/restrpc/meals/{id}")
    Meal getMealById(@PathVariable String id) {
        Optional<Meal> meal = mealsRepository.findMeal(id);

        return meal.orElseThrow(() -> new MealNotFoundException(id));
    }

    @GetMapping("/restrpc/meals")
    Collection<Meal> getMeals() {
        return mealsRepository.getAllMeal();
    }

    @GetMapping("/restrpc/meals/biggest")
    Meal getBiggestMeal() {
        Collection<Meal> meals = mealsRepository.getAllMeal();
        Meal biggestMeal = null;
        for(Meal m : meals){
            if(biggestMeal == null || biggestMeal.getKcal()<m.getKcal()){
                biggestMeal = m;
            }
        }
        return biggestMeal;
    }

    @GetMapping("/restrpc/meals/cheapest")
    Meal getCheapestMeal() {
        Collection<Meal> meals = mealsRepository.getAllMeal();
        Meal cheapestMeal = null;
        for(Meal m : meals){
            if(cheapestMeal == null || cheapestMeal.getPrice()>m.getPrice()){
                cheapestMeal = m;
            }
        }
        return cheapestMeal;
    }

    @PostMapping("/restrpc/meals")
    int addMeal(@RequestBody Meal newMeal){
        mealsRepository.saveMeal(newMeal);
        return HttpStatus.CREATED.value(); //201
    }

    @PutMapping("restrpc/meals/{id}")
    int updateMeal(@RequestBody Meal newMeal, @PathVariable String id){
        Meal updatedMeal = mealsRepository.findMeal(id).map(meal -> {
            meal.setName(newMeal.getName());
            meal.setKcal(newMeal.getKcal());
            meal.setMealType(newMeal.getMealType());
            meal.setDescription(newMeal.getDescription());
            meal.setPrice(newMeal.getPrice());
            return meal;
        }).orElseThrow(()->new MealNotFoundException(id));
        mealsRepository.saveMeal(updatedMeal);
        return HttpStatus.ACCEPTED.value(); //202
    }

    @DeleteMapping("restrpc/meals/{id}")
    int deleteMeal(@PathVariable String id){
        Meal meal = mealsRepository.findMeal(id).orElseThrow(() -> new MealNotFoundException(id));
        mealsRepository.deleteMeal(meal.getId());
        return HttpStatus.OK.value(); //200
    }
}
