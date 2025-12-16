package Adesk_CounterpartyService.Models.Counterparty;

import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CounterpartyModelDTO {
    @JsonProperty("CompanyId")
    private Long companyId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("CategoryName")
    public String categoryName;

    @JsonProperty("Description")
    public String description;

    @JsonProperty("Email")
    public String email;

    @JsonProperty("Number")
    public String number;

    @JsonIgnore
    public boolean isValid(){
        return companyId != 0 &&
                name != null && !name.trim().isEmpty() &&
                categoryName != null && categoryName.trim().isEmpty() &&
                description != null && description.trim().isEmpty() &&
                email != null && email.trim().isEmpty() &&
                number != null && number.trim().isEmpty();
    }
}
