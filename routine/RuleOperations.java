package routine;

import routine.Rule;

import java.util.HashSet;
import java.util.List;

public class RuleOperations {

    public static boolean isIntersect(Rule r1, Rule r2)
    {
        for (int i = 0; i < r1.bits.length; i++)
        {
            if (r1.bits[i] != r2.bits[i] && r1.bits[i] != '*' && r2.bits[i] != '*')
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isCover(Rule r1, Rule r2) {
        if (r1.stars < r2.stars)
        {
            return false;
        }
        if (r1.zeros > r2.zeros)
        {
            return false;
        }
        for (int i = 0; i < r1.bits.length; i++)
        {
            if (r1.bits[i] != r2.bits[i] && r1.bits[i] != '*')
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isResolute(Rule r1, Rule r2)
    {
        if (r1.stars != r2.stars)
        {
            return false;
        }
        if (Math.abs(r1.zeros -r2.zeros) != 1)
        {
            return false;
        }

        int cnt = 0;
        for (int i = 0; i < r1.bits.length; i++)
        {
            if (r1.bits[i] != r2.bits[i] && (r1.bits[i] == '*' || r2.bits[i] == '*'))
            {
                return false;
            }
            if (r1.bits[i] != r2.bits[i])
            {
                cnt++;
            }
            if (cnt > 1)
            {
                return false;
            }
        }
        return cnt==1;
    }

    public static boolean hasCommonActions(Rule r1, Rule r2)
    {
        return r1.actions.stream().anyMatch(r2.actions::contains);
    }

    public static Rule createRuleByResolutionWithoutActions(Rule r1, Rule r2)
    {
        if (!isResolute(r1, r2))
        {
            throw new AssertionError();
        }

        Rule result = new Rule(r1.bits, r1.prioroty, new HashSet<>());
        for (int i = 0; i < r2.bits.length; i++)
        {
            if (r1.bits[i] != r2.bits[i])
            {
                result.modifyBit('*', i);
            }
        }
        return result;
    }

    public static Rule createRuleByResolution(Rule r1, Rule r2, List<Rule> r2Dependend)
    {
        HashSet<Integer> newSet = new HashSet<Integer>(r1.actions);
        for (Rule rt : r2Dependend)
        {
            newSet.retainAll(rt.actions);
        }
        newSet.retainAll(r2.actions);

        if (newSet.size() == 0)
        {
            return null;
        }
        Rule result =  createRuleByResolutionWithoutActions(r1, r2);
        result.actions.addAll(newSet);
        return result;
    }
}
