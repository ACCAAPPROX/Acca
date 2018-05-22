package routine;

import java.util.HashSet;

public class Rule {

    public Rule(String bits, int prioroty)
    {
        this.bits = bits.toCharArray();
        this.prioroty = prioroty;
        this.actions = new HashSet<>();
        actions.add(0);
        calcStars();
    }

    public Rule(char[] bits, int prioroty, HashSet<Integer> actions)
    {
        this.bits = bits.clone();
        this.prioroty = prioroty;
        this.actions = actions;
        calcStars();
    }

    public Rule(Rule r)
    {
        this.bits = r.bits.clone();
        this.prioroty = r.prioroty;
        this.actions = new HashSet<>();
        calcStars();
    }


    public void modifyBit(char val, int index){
        bits[index] = val;
        calcStars();
    }

    private void calcStars()
    {
        stars = 0;
        zeros = 0;
        for (int i = 0; i < bits.length; i++)
        {
            if (bits[i] == '*')
            {
                stars++;
            }

            if (bits[i] == '0')
            {
                zeros++;
            }

        }
    }

    public String toString()
    {
        return toBitString() + " "+actions.toString();
    }

    public String toBitString()
    {
        String ans = "";
        for (int i = 0; i < bits.length; i++)
        {
            ans += bits[i];
        }
        return ans;
    }


    public char[] bits;
    int prioroty;
    public int stars;
    int zeros;

    public HashSet<Integer> actions;

}
