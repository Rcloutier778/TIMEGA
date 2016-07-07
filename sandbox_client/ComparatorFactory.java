package sandbox_client;

import java.util.Comparator;

public class ComparatorFactory {
	
	private static final ComparatorFactory _factory = new ComparatorFactory();

	public static Comparator<String> generatePersonnelComparator() {
		return _factory.new PersonnelComparator();
	}
	
	public static Comparator<String> generatePersonnelComparator2() {
		return _factory.new PersonnelComparator2();
	}
	
	public static Comparator<String> generateTechnologyComparator() {
		return _factory.new TechnologyComparator();
	}
	
	// used to order personnel on the personnel page
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
	
	// used to order personnel on the players page
	private class PersonnelComparator2 implements Comparator<String> {
		
		@Override
		public int compare(String o1, String o2) {
			return Database.comparePersonnel(o1, o2);
		}
	}
	
	private class TechnologyComparator implements Comparator<String> {
		
		@Override
		public int compare(String o1, String o2) {
			return Database.compareTech(o1, o2);
		}
	}
	
}
