/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.flowable.dmn.engine.impl.hitpolicy;

import org.flowable.dmn.engine.impl.context.Context;
import org.flowable.dmn.engine.impl.mvel.MvelExecutionContext;
import org.flowable.dmn.model.HitPolicy;
import org.flowable.engine.common.api.FlowableException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Yvo Swillens
 */
public class HitPolicyAny extends AbstractHitPolicy implements ComposeDecisionResultBehavior {

    @Override
    public String getHitPolicyName() {
        return HitPolicy.ANY.getValue();
    }

    public void composeDecisionResults(final MvelExecutionContext executionContext) {
        if (Context.getDmnEngineConfiguration().isStrictMode()) {

            for (Map.Entry<Integer, Map<String, Object>> ruleResults : executionContext.getRuleResults().entrySet()) {

                for (Map.Entry<Integer, Map<String, Object>> otherRuleResults : executionContext.getRuleResults().entrySet()) {

                    if (!otherRuleResults.getKey().equals(ruleResults.getKey())) {

                        for (Map.Entry<String, Object> outputValues : otherRuleResults.getValue().entrySet()) {
                            if (!ruleResults.getValue().containsKey(outputValues.getKey()) ||
                                (ruleResults.getValue().containsKey(outputValues.getKey()) && !outputValues.getValue().equals(ruleResults.getValue().get(outputValues.getKey())))) {
                                throw new FlowableException(
                                    String.format("HitPolicy: %s; both rule %d and %d are valid but output %s has different values",
                                        getHitPolicyName(), otherRuleResults.getKey(), ruleResults.getKey(), outputValues.getKey()));
                            }
                        }
                    }
                }
            }
        }

        List<Map<String, Object>> ruleResults = new ArrayList<>(executionContext.getRuleResults().values());
        if (!ruleResults.isEmpty()) {
            executionContext.setDecisionResults(Arrays.asList(ruleResults.get(ruleResults.size() - 1)));
        }
    }

}