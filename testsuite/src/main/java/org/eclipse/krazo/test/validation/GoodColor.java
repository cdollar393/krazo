package org.eclipse.krazo.test.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {GoodColorValidator.class})
@Documented
public @interface GoodColor {

    String message() default "That is not a good color";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
