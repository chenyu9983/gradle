/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.deprecation;

import com.google.common.base.Joiner;
import org.gradle.internal.featurelifecycle.DeprecatedFeatureUsage;

import java.util.List;

import static org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler.getRemovalDetails;
import static org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler.getWillBecomeErrorMessage;

public class DeprecationMessage {

    private String summary;
    private String removalDetails;
    private String advice;
    private String contextualAdvice;
    private String documentationReference;

    private DeprecatedFeatureUsage.Type usageType = DeprecatedFeatureUsage.Type.USER_CODE_DIRECT;

    // Output: ${summary}. This has been deprecated and is scheduled to be removed in Gradle {X}.
    public static DeprecationMessage thisHasBeenDeprecated(final String summary) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(summary);
                removalDetails(String.format("This has been deprecated and %s", getRemovalDetails()));
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Output: ${thing} has been deprecated. This is scheduled to be removed in Gradle {X}.
    public static DeprecationMessage specificThingHasBeenDeprecated(final String thing) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("%s has been deprecated.", thing));
                removalDetails(thisWillBeRemovedMessage());
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Output: ${thing} has been deprecated. This is scheduled to be removed in Gradle {X}.
    public static DeprecationMessage indirectCodeUsageHasBeenDeprecated(String thing) {
        return specificThingHasBeenDeprecated(thing).withIndirectUsage();
    }

    // Output: ${behaviour}. This behaviour has been deprecated and is scheduled to be removed in Gradle {X}.
    public static DeprecationMessage behaviourHasBeenDeprecated(final String behaviour) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(behaviour);
                removalDetails(String.format("This behaviour has been deprecated and %s", getRemovalDetails()));
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Output: ${feature} has been deprecated. This is scheduled to be removed in Gradle {X}.
    public static DeprecationMessage deprecatedBuildInvocationFeature(final String feature) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("%s has been deprecated.", feature));
                removalDetails(thisWillBeRemovedMessage());
                withBuildInvocation();
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Output: The ${parameterName} named parameter has been deprecated. This is scheduled to be removed in Gradle {X}. Please use the ${replacement} named parameter instead.
    public static DeprecationMessage replacedNamedParameter(final String parameterName, final String replacement) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("The %s named parameter has been deprecated.", parameterName));
                removalDetails(thisWillBeRemovedMessage());
                withAdvice(String.format("Please use the %s named parameter instead.", replacement));
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Output: The ${propertyName} property has been deprecated. This is scheduled to be removed in Gradle {X}.
    public static DeprecationMessage deprecatedProperty(final String propertyName) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("The %s property has been deprecated.", propertyName));
                removalDetails(thisWillBeRemovedMessage());
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Output: The ${propertyName} property has been deprecated. This is scheduled to be removed in Gradle {X}. Please use the ${replacement} property instead.
    public static DeprecationMessage replacedProperty(final String propertyName, final String replacement) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("The %s property has been deprecated.", propertyName));
                removalDetails(thisWillBeRemovedMessage());
                withAdvice(String.format("Please use the %s property instead.", replacement));
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    public static DeprecationMessage replacedConfiguration(final String configurationName, final ConfigurationDeprecationType deprecationType, final List<String> replacements) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("The %s configuration has been deprecated for %s.", configurationName, deprecationType.displayName()));
                removalDetails(getWillBecomeErrorMessage());
                withAdvice(String.format("Please %s the %s configuration instead.", deprecationType.usage, Joiner.on(" or ").join(replacements)));
                if (!deprecationType.inUserCode) {
                    withIndirectUsage();
                }
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Output: The ${methodName} method has been deprecated. This is scheduled to be removed in Gradle {X}.
    public static DeprecationMessage discontinuedMethod(final String methodName) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("The %s method has been deprecated.", methodName));
                removalDetails(thisWillBeRemovedMessage());
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Output: The ${methodName} method has been deprecated. This is scheduled to be removed in Gradle {X}. Please use the ${replacement} method instead.
    public static DeprecationMessage replacedMethod(final String methodName, final String replacement) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("The %s method has been deprecated.", methodName));
                removalDetails(thisWillBeRemovedMessage());
                withAdvice(String.format("Please use the %s method instead.", replacement));
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Output: Using method ${methodName} has been deprecated. This will fail with an error in Gradle {X}.
    public static DeprecationMessage discontinuedMethodInvocation(final String invocation) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("Using method %s has been deprecated.", invocation));
                removalDetails(thisWillBecomeAnError());
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Use for some operation that is not deprecated, but something about the method parameters or state is deprecated.
    // Output: Using method ${methodName} has been deprecated. This will fail with an error in Gradle {X}. Please use the ${replacement} method instead.
    public static DeprecationMessage replacedMethodInvocation(final String methodName, final String replacement) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("Using method %s has been deprecated.", methodName));
                removalDetails(thisWillBecomeAnError());
                withAdvice(String.format("Please use the %s method instead.", replacement));
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    // Use for some operation that is not deprecated, but something about the method parameters or state is deprecated.
    // Output: ${invocation} has been deprecated. This will fail with an error in Gradle {X}.
    public static DeprecationMessage discontinuedInvocation(final String invocation) {
        return new DeprecationMessage() {
            @Override
            public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
                summary(String.format("%s has been deprecated.", invocation));
                removalDetails(thisWillBecomeAnError());
                return super.toDeprecatedFeatureUsage(calledFrom);
            }
        };
    }

    public DeprecationMessage(String summary, String removalDetails) {
        this.summary = summary;
        this.removalDetails = removalDetails;
    }

    protected DeprecationMessage() {
    }

    protected void summary(String summary) {
        this.summary = summary;
    }

    protected void removalDetails(String removalDetails) {
        this.removalDetails = removalDetails;
    }

    public DeprecationMessage withAdvice(String advice) {
        this.advice = advice;
        return this;
    }

    public DeprecationMessage withContextualAdvice(String contextualAdvice) {
        this.contextualAdvice = contextualAdvice;
        return this;
    }

    public DeprecationMessage withDocumentationReference(String documentationReference) {
        this.documentationReference = documentationReference;
        return this;
    }

    public DeprecationMessage withIndirectUsage() {
        this.usageType = DeprecatedFeatureUsage.Type.USER_CODE_INDIRECT;
        return this;
    }

    public DeprecationMessage withBuildInvocation() {
        this.usageType = DeprecatedFeatureUsage.Type.BUILD_INVOCATION;
        return this;
    }

    public DeprecatedFeatureUsage toDeprecatedFeatureUsage(Class<?> calledFrom) {
        return new DeprecatedFeatureUsage(summary, removalDetails, advice, contextualAdvice, usageType, calledFrom);
    }

    static String thisWillBeRemovedMessage() {
        return String.format("This %s", getRemovalDetails());
    }

    private static String thisWillBecomeAnError() {
        return getWillBecomeErrorMessage();
    }

}
