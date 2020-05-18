package org.eclipse.krazo.test.validation;

import org.eclipse.krazo.binding.validate.KrazoValidated;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.FormParam;

@KrazoValidated
@GoodColor
public class ColorFormModel {

    private String colorInput;

    @NotBlank
    public String getColorInput() {
        return colorInput;
    }

    @FormParam("color")
    public void setColorInput(String colorInput) {
        this.colorInput = colorInput;
    }

    @Override
    public String toString() {
        return colorInput;
    }
}
