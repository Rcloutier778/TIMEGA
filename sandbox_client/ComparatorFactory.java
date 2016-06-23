package sandbox_client;

import java.util.Comparator;

public class ComparatorFactory {

	public static Comparator<String> generatePersonnelComparator() {
		return new ComparatorFactory().new PersonnelComparator();
	}
	
	private class PersonnelComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			int tier1 = Database.tierOfPersonnel(o1);
			int tier2 = Database.tierOfPersonnel(o2);
			if(tier1 == tier2)
				return o1.compareTo(o2);
			return tier1 - tier2;
		}
		
	}
	
}
