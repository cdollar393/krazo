package org.eclipse.krazo.test.validation;

import javax.inject.Inject;
import javax.mvc.Controller;
import javax.mvc.binding.BindingResult;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

@Controller
@Path("/color")
public class ColorController {

    @Inject
    private BindingResult br;

    @Context
    private HttpServletRequest request;

    @Inject
    private ColorEjb colorEjb;

    @GET
    @Path("/basic")
    public String getColorFormBasic() {
        return "form.jsp";
    }

    @POST
    @Path("/basic")
    public String processColorFormBasic(@NotNull @Valid @BeanParam ColorFormModel form) {

        if(br.isFailed()) {
            request.setAttribute("errors", br.getAllErrors());
            return "error.jsp";
        }

        return "success.jsp";
    }

    @GET
    @Path("/ejb")
    public String getColorFormEjb() {
        return "form.jsp";
    }

    @POST
    @Path("/ejb")
    public String processColorFormWithEjb(@NotNull @Valid @BeanParam ColorFormModel form) {

        if(br.isFailed()) {
            request.setAttribute("errors", br.getAllErrors());
            return "error.jsp";
        }

        colorEjb.doEjbStuff(form);

        return "success.jsp";
    }
}
