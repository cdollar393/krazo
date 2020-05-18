package org.eclipse.krazo.binding.validate;

/**
 * The type of validation to be used with the {@link KrazoValidated} annotation.
 */
public enum KrazoValidatedScope {

    /**
     * Consider constraints at the bean type level only.
     */
    TYPE_ONLY,

    /**
     * Consider constraints at field and field accessor method level only.
     */
    FIELDS_ONLY,

    /**
     * Consider constraints for the bean type level and all JAX-RS annotation fields and methods.
     */
    ALL
}
