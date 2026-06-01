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

class Solution {
    public int[] constructTransformedArray(int[] nums) {
        int n = nums.length;
        int[] result = new int[];
        // List<Integer> result = new ArrayList();
        for(int i=0;i<n;i++){
            int no = nums[i];
            if(no>0){
                no=i+no%n;
            }else{
                no=i+n-abs(no%n);
            }
           result[i] = nums[no];

        }
        return result;
    }
}

class Solution {
    int min = Integer.MAX_VALUE;
    public int minRemoval(int[] nums, int k) {
        Arrays.sort(nums);
        int i=0;
        int j=1;

        while(j<nums.length){
            max = arr[j];
            min = arr[i];
            if(max<=min*k){
                
            }else{

            }
        }

        return min;
    }

    public int solve(int i,int j,int[] arr,int k){
        max = arr[j];
        min = arr[i];

        if(max>(k*min)){
            return n;
        }else{
           return  
        }
    }

}
class Solution {
    public int longestValidParentheses(String s) {
        if
    }
}

