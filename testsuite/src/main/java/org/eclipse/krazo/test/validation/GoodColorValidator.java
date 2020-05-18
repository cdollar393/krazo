package org.eclipse.krazo.test.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class GoodColorValidator implements ConstraintValidator<GoodColor, ColorFormModel> {

    private final static List<String> GOOD_COLORS = Arrays.asList("red", "orange", "yellow");

    @Override
    public boolean isValid(ColorFormModel colorFormModel, ConstraintValidatorContext constraintValidatorContext) {
        return colorFormModel == null
            || colorFormModel.getColorInput() == null
            || colorFormModel.getColorInput().trim().isEmpty()
            || GOOD_COLORS.contains(colorFormModel.getColorInput().toLowerCase());
    }
}
