package mainPackage;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;

@Getter
public class transitionModel {
    private List<Long> prev;
    private HashMap<Long, Long> curr;

    transitionModel(){

    }

    @Override
    public String toString() {
        return super.toString();
    }
}
