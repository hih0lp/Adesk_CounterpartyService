package Adesk_CounterpartyService.Models.Category;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CounterpartyCategoryDTO {
    @JsonProperty("Name")
    public String name;

    public boolean isValid(){
        return name != null && !name.trim().isEmpty();
    }
}
