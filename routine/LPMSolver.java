package routine;

import javafx.util.Pair;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.*;

public class LPMSolver
{
    class Node
    {
        Node leftSon;
        Node rightSon;

        public Node()
        {
            avaliableActions = new HashSet<>();
        }

        HashSet<Integer> avaliableActions;
    }

    public Pair<Integer, HashSet<Integer>> dfs(Node cur, HashSet<Integer> up, String path)
    {
        if (!cur.avaliableActions.isEmpty())
        {
           up = cur.avaliableActions;
        }
        if (cur.leftSon == null)
        {
            if (cur.rightSon != null)
            {
                throw new AssertionError();
            }
            return new Pair<>(1, new HashSet<>(up));
        }

        Pair<Integer, HashSet<Integer>> ansL = dfs(cur.leftSon, up, path+"0");
        Pair<Integer, HashSet<Integer>> ansR = dfs(cur.rightSon, up, path+"1");

        HashSet<Integer> ansActions = new HashSet<>(ansL.getValue());
        int ans = ansL.getKey() + ansR.getKey();

        if (ansL.getValue().stream().anyMatch(ansR.getValue()::contains))
        {
            ansActions.retainAll(ansR.getValue());
            ans--;
        } else
        {
            ansActions.addAll(ansR.getValue());
        }

        if (ansActions.size() == 0)
        {
            throw new AssertionError();
        }

        return new Pair<>(ans, ansActions);
    }



    public int calculateNumberOfRules(ArrayList<Rule> rules)
    {

        Node root = new Node();
        for (Rule r : rules)
        {
            Node cur = root;
            for (int i = 0; i < r.bits.length; i++)
            {
                if (r.bits[i] == '*')
                {
                    for (int j = i; j < r.bits.length; j++)
                    {
                        if (r.bits[j] != '*')
                        {
                            throw new AssertionError();
                        }
                    }
                    break;
                }
                if (cur.leftSon == null)
                {
                    cur.leftSon = new Node();
                    cur.rightSon = new Node();
                }
                if (r.bits[i] == '0')
                {
                    cur = cur.leftSon;
                }
                else
                {
                    cur = cur.rightSon;
                }

            }
            cur.avaliableActions.addAll(r.actions);
        }

        HashSet<Integer> start = new HashSet<Integer>();
        start.add(-1);
        return dfs(root, start,"").getKey();

    }
}
