/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.server.spi.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import io.onedev.server.util.jackson.hibernate.EntityDeserializer;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.model.Parameterized;

/**
 * Utility methods for retrieving values or value providers for the
 * {@link Parameterized parameterized} resource model components.
 *
 * @author Marek Potociar
 */
public final class ParameterValueHelper {

    /**
     * Get the array of parameter values.
     *
     * @param valueProviders a list of value providers.
     * @return array of parameter values provided by the value providers.
     */
    public static Object[] getParameterValues(List<ParamValueFactoryWithSource<?>> valueProviders, ContainerRequest request) {
        final Object[] params = new Object[valueProviders.size()];
		EntityDeserializer.pushParams(params);
        try {
            int entityProviderIndex = -1;
            int index = 0;

            for (ParamValueFactoryWithSource<?> paramValProvider : valueProviders) {
                // entity provider has to be called last; see JERSEY-2642
                if (paramValProvider.getSource().equals(Parameter.Source.ENTITY)) {
                    entityProviderIndex = index++;
                    continue;
                }

                params[index++] = paramValProvider.apply(request);
            }

            if (entityProviderIndex != -1) {
                params[entityProviderIndex] = valueProviders.get(entityProviderIndex).apply(request);
            }

            return params;
        } catch (WebApplicationException e) {
            throw e;
        } catch (MessageBodyProviderNotFoundException e) {
            throw new NotSupportedException(e);
        } catch (ProcessingException e) {
            throw e;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof WebApplicationException) {
                throw (WebApplicationException) e.getCause();
            }

            throw new MappableException("Exception obtaining parameters", e);
        } finally {
			EntityDeserializer.popParams();
		}
    }

    /**
     * Create list of parameter value providers for the given {@link Parameterized
     * parameterized} resource model component.
     *
     * @param valueSuppliers all registered value suppliers.
     * @param parameterized  parameterized resource model component.
     * @return list of parameter value providers for the parameterized component.
     */
    public static List<ParamValueFactoryWithSource<?>> createValueProviders(Collection<ValueParamProvider> valueSuppliers,
            Parameterized parameterized) {
        if ((null == parameterized.getParameters()) || (0 == parameterized.getParameters().size())) {
            return Collections.emptyList();
        }

        List<ValueParamProvider> valueParamProviders = valueSuppliers.stream()
                        .sorted((o1, o2) -> o2.getPriority().getWeight() - o1.getPriority().getWeight())
                        .collect(Collectors.toList());

        boolean entityParamFound = false;
        final List<ParamValueFactoryWithSource<?>> providers = new ArrayList<>(parameterized.getParameters().size());
        for (final Parameter parameter : parameterized.getParameters()) {
            final Parameter.Source parameterSource = parameter.getSource();
            entityParamFound = entityParamFound || Parameter.Source.ENTITY == parameterSource;
            final Function<ContainerRequest, ?> valueFunction = getParamValueProvider(valueParamProviders, parameter);
            if (valueFunction != null) {
                providers.add(wrapParamValueProvider(valueFunction, parameterSource));
            } else {
                providers.add(null);
            }
        }

        if (!entityParamFound && Collections.frequency(providers, null) == 1) {
            // Try to find entity if there is one unresolved parameter and the annotations are unknown
            final int entityParamIndex = providers.lastIndexOf(null);
            final Parameter parameter = parameterized.getParameters().get(entityParamIndex);
            if (Parameter.Source.UNKNOWN == parameter.getSource() && !parameter.isQualified()) {
                final Parameter overriddenParameter = Parameter.overrideSource(parameter, Parameter.Source.ENTITY);
                final Function<ContainerRequest, ?> valueFunction = getParamValueProvider(
                        valueParamProviders,
                        overriddenParameter);
                if (valueFunction != null) {
                    providers.set(entityParamIndex, wrapParamValueProvider(valueFunction, overriddenParameter.getSource()));
                } else {
                    providers.set(entityParamIndex, null);
                }
            }
        }

        return providers;
    }

    private static <T> ParamValueFactoryWithSource<T> wrapParamValueProvider(
            Function<ContainerRequest, T> factory, Parameter.Source paramSource) {
        return new ParamValueFactoryWithSource<>(factory, paramSource);
    }

    private static Function<ContainerRequest, ?> getParamValueProvider(
            Collection<ValueParamProvider> valueProviders, final Parameter parameter) {
        Function<ContainerRequest, ?> valueProvider = null;
        Iterator<ValueParamProvider> vfpIterator = valueProviders.iterator();
        while (valueProvider == null && vfpIterator.hasNext()) {
            valueProvider = vfpIterator.next().getValueProvider(parameter);
        }
        return valueProvider;
    }

    /**
     * Prevents instantiation.
     */
    private ParameterValueHelper() {
    }
}
