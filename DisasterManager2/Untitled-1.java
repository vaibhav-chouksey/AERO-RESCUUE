import java.util.ArrayList;
import java.util.List;

class Solution {
    int count=0;
    public int minimumPairRemoval(int[] nums) {
         List<Integer> ls = new ArrayList<>();
         for(int i=0;i<nums.length;i++){
            ls.add(nuums[i]);
         }
         solve(ls);
        return count;
    }
    public void solve(List<Integer> list){
        int minInd = 0;
        Boolean flag = true;
        int minSum = Integer.MAX_VALUE;
        for(int i=0;i<list.size()-1;i++){
            if((list.get(i)+list.get(i+1))<minSum){
                minSum = (list.get(i)+list.get(i+1));
                minInd = i;
            }
            if(list.get(i)>list.get(i+1)){
                flag =false;
            }
        }
        if(flag) return;
        List<Integer> ls = new ArrayList<>();
        count++;
        for(int i=0;i<list.size();i++){
            if(i!=minInd && i != minInd+1){
                ls.add(list.get(i));
            }
        }
        solve(ls);
    }
}