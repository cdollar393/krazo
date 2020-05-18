package org.eclipse.krazo.binding.validate;

import javax.ws.rs.core.Response;
import java.lang.annotation.*;

/**
 * This annotation is used to enable MVC-specific bean validation rules for a JAX-RS parameter
 * binding. It is similar to {@link javax.mvc.binding.MvcBinding}, except that this annotation
 * focuses only on bean validation and does not deal with value conversion.
 * <p>
 * This annotation is allowed at the type or class level. When used at type level with no
 * arguments then all JAX-RS annotated fields such as {@link javax.ws.rs.FormParam} and
 * {@link javax.ws.rs.QueryParam} will be treated as though they were also annotated with
 * {@link javax.mvc.binding.MvcBinding}. Any constraints placed at the type level on the
 * {@link KrazoValidated} bean class will also be included in the {@link javax.mvc.binding.BindingResult}.
 * If a type-level constraint fails then it will be added to the binding result with a null
 * parameter name.
 *
 * @see javax.mvc.binding.MvcBinding
 * @see javax.mvc.binding.BindingResult
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface KrazoValidated {

    /**
     * The validation scope, which defaults to {@link KrazoValidatedScope#ALL}.
     *
     * @return the validation scope
     */
    KrazoValidatedScope validationScope() default KrazoValidatedScope.ALL;
}
