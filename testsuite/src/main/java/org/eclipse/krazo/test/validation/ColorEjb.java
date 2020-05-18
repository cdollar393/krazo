package org.eclipse.krazo.test.validation;

import javax.ejb.Stateless;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Stateless
public class ColorEjb {

    public void doEjbStuff(final @NotNull @Valid ColorFormModel form) {
        System.out.println(form);
    }
}
