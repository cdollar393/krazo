/*
 * Copyright (c) 2018, 2019 Eclipse Krazo committers and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.krazo.binding.validate;

import javax.mvc.binding.MvcBinding;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Utility class to create {@link ConstraintViolationMetadata} from constraint violations.
 *
 * @author Christian Kaltepoth
 */
public class ConstraintViolations {

    private static final Logger log = Logger.getLogger(ConstraintViolations.class.getName());

    private ConstraintViolations() {
        // utility class
    }

    public static ConstraintViolationMetadata getMetadata(ConstraintViolation<?> violation) {

        final ViolatedObject violatedObject = getViolatedObjectDetails(violation);

        return new ConstraintViolationMetadata(violation, violatedObject.annotations, violatedObject.mvcBoundViolation);

    }

    private static boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> type) {
        return Arrays.stream(annotations).anyMatch(a -> a.annotationType().equals(type));
    }

    private static ViolatedObject getViolatedObjectDetails(ConstraintViolation<?> violation) {

        // create a simple list of nodes from the path
        List<Path.Node> nodes = new ArrayList<>();
        for (Path.Node node : violation.getPropertyPath()) {
            nodes.add(node);
        }
        Path.Node lastNode = nodes.get(nodes.size() - 1);

        // the path refers to some property of the leaf bean
        if (lastNode.getKind() == ElementKind.PROPERTY) {

            Path.PropertyNode propertyNode = lastNode.as(Path.PropertyNode.class);

            Annotation[] annotations = getPropertyAnnotations(violation, propertyNode);

            // the property is MVC bound for validation if it has an @MvcBinding annotation
            // or if the leaf bean is annotated with @KrazoValidated with the FIELDS_ONLY or
            // ALL scope
            KrazoValidated krazoValidated = extractViolationClass(violation.getLeafBean().getClass()).getAnnotation(KrazoValidated.class);
            boolean mvcBoundConstraint = hasAnnotation(annotations, MvcBinding.class)
                || (krazoValidated != null && (krazoValidated.validationScope() == KrazoValidatedScope.ALL || krazoValidated.validationScope() == KrazoValidatedScope.FIELDS_ONLY));

            return new ViolatedObject(annotations, mvcBoundConstraint);

        }

        // The path refers to a method parameter
        else if (lastNode.getKind() == ElementKind.PARAMETER && nodes.size() == 2) {

            Path.MethodNode methodNode = nodes.get(0).as(Path.MethodNode.class);
            Path.ParameterNode parameterNode = nodes.get(1).as(Path.ParameterNode.class);

            return new ViolatedObject(getParameterAnnotations(violation, methodNode, parameterNode), false);

        }

        // The path refers to bean-level constraint on a method parameter
        else if (lastNode.getKind() == ElementKind.BEAN && nodes.size() == 3) {

            Class<?> invalidClass = extractViolationClass(violation.getInvalidValue().getClass());

            Annotation[] annotations = invalidClass.getAnnotations();

            // the property is MVC bound for validation if the invalid class is annotated with @KrazoValidated
            // with the TYPE_ONLY or ALL scope
            KrazoValidated krazoValidated = invalidClass.getAnnotation(KrazoValidated.class);
            boolean mvcBoundConstraint = hasAnnotation(annotations, MvcBinding.class)
                || (krazoValidated != null && (krazoValidated.validationScope() == KrazoValidatedScope.ALL || krazoValidated.validationScope() == KrazoValidatedScope.TYPE_ONLY));

            return new ViolatedObject(annotations, mvcBoundConstraint);

        }


        log.warning("Could not read annotations for path: " + violation.getPropertyPath().toString());
        return new ViolatedObject(new Annotation[0], false);

    }

    /**
     * Extracts and returns the un-proxied parent class for the constraint
     * violation.
     * <p>
     * Background: Under Weld CDI the invalid value class will likely be a proxy
     * class instead of the real class. We need the real class, as the Weld
     * proxy won't include the same annotations as the real class. In the case
     * of a proxy class the invalid value class will be synthetic, and the
     * superclass will be the real class that we're interested in.
     *
     * @return the violated parent class
     */
    private static Class<?> extractViolationClass(final Class<?> possiblyProxiedClass) {
        return possiblyProxiedClass.isSynthetic() ? possiblyProxiedClass.getSuperclass() : possiblyProxiedClass;
    }

    private static Annotation[] getPropertyAnnotations(ConstraintViolation<?> violation, Path.PropertyNode node) {

        Class<?> leafBeanClass = extractViolationClass(violation.getLeafBean().getClass());
        Set<Annotation> allAnnotations = new HashSet<>();
        try {

            Field field = leafBeanClass.getDeclaredField(node.getName());
            allAnnotations.addAll(Arrays.asList(field.getAnnotations()));

        } catch (NoSuchFieldException e) {
            // ignore for now
        }

        allAnnotations.addAll(readAndWriteMethodAnnotationsForField(leafBeanClass, node.getName()));

        return allAnnotations.toArray(new Annotation[0]);
    }

    private static Annotation[] getParameterAnnotations(ConstraintViolation<?> violation, Path.MethodNode methodNode,
                                                        Path.ParameterNode parameterNode) {

        try {

            String methodName = methodNode.getName();

            int paramCount = methodNode.getParameterTypes().size();
            Class[] paramTypes = methodNode.getParameterTypes().toArray(new Class[paramCount]);

            Class<?> rootBeanClass = violation.getRootBean().getClass();
            Method method = rootBeanClass.getMethod(methodName, paramTypes);

            int parameterIndex = parameterNode.getParameterIndex();
            return method.getParameterAnnotations()[parameterIndex];

        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Returns a set of all annotations present on the getter and setter methods
     * for field fieldName in class beanClass. The bean class must be a valid
     * java bean.
     *
     * @param beanClass the bean class
     * @param fieldName the field in the bean class
     * @return a set of all annotations on the read and write methods for the
     * field, or an empty set if none are found
     */
    private static Set<Annotation> readAndWriteMethodAnnotationsForField(Class<?> beanClass, String fieldName) {
        Set<Annotation> annotationsSet = new HashSet<>();

        try {

            BeanInfo info = Introspector.getBeanInfo(beanClass);

            Optional<PropertyDescriptor> descriptorOpt = Arrays.stream(info.getPropertyDescriptors())
                .filter(desc -> desc.getName().equals(fieldName))
                .findFirst();

            if(descriptorOpt.isPresent()) {

                Method getter = descriptorOpt.get().getReadMethod();
                if(getter != null) {
                    annotationsSet.addAll(Arrays.asList(getter.getAnnotations()));
                }

                Method setter = descriptorOpt.get().getWriteMethod();
                if(setter != null) {
                    annotationsSet.addAll(Arrays.asList(setter.getAnnotations()));
                }

            }
        }
        catch(IntrospectionException e) {
            log.warning(
                String.format("Unable to introspect read and write methods for field '%s' on bean class '%s': %s",
                    fieldName, beanClass.getName(), e.getMessage()
                )
            );
        }

        return annotationsSet;
    }

    private static class ViolatedObject {

        private final Annotation[] annotations;
        private final boolean mvcBoundViolation;

        ViolatedObject(final Annotation[] annotations, final boolean mvcBoundViolation) {
            this.annotations = annotations;
            this.mvcBoundViolation = mvcBoundViolation;
        }
    }
}
