package me.momarija.bioui.services.impl;

import me.momarija.bioui.domains.Donnee;
import me.momarija.bioui.domains.Engin;
import me.momarija.bioui.repos.ChantierRepo;
import me.momarija.bioui.repos.DonneeRepo;
import me.momarija.bioui.repos.EnginRepo;
import me.momarija.bioui.services.UserService;
import me.momarija.bioui.services.algo.DateUtility;
import me.momarija.bioui.services.algo.TraitementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private ChantierRepo chantierRepo;

	@Autowired
	private EnginRepo enginRepo;

	@Autowired
	private DonneeRepo donneeRepo;

	@Autowired
	private TraitementService traitementService;

	private DateUtility dateUtility = new DateUtility();

	@Override
	public Map<String, String> getEnginStatistics(int enginId, Date from, Date to) {
		Engin engin = enginRepo.findOne(enginId);
		Map<String, Integer> map = doWork(engin, from, to);
		Map<String, String> map2 = new HashMap<>();


		//because the time ( arret ) cannot be saved when the <engin> is not in travel
		long a  = dateUtility.convertToTime(from,to);

		System.out.println(a);
		long arret  = a/1000 - map.get("arret") - map.get("production") - map.get("ralenti");


		map2.put("production", dateUtility.convertToDate(map.get("production")));
		map2.put("ralenti", dateUtility.convertToDate(map.get("ralenti")));
		map2.put("arret", dateUtility.convertToDate((int)arret));

		return map2;
	}

	@Override
	public Map<String, String> getChantierStatistics(int chantierId, Date from, Date to) {
		List<Engin> list = chantierRepo.findOne(chantierId).getEngins();
		int p=0,r=0,a=0;
		Map<String, Integer> mapEngin= new HashMap<>();
		Map<String, Integer> map;
		for(Engin engin:list){
			map = doWork(engin,from,to);
			p = p+ map.get("production");
			r = r+ map.get("ralenti");
			a = a+ map.get("arret");
		}
		mapEngin.put("production",p/list.size());
		mapEngin.put("ralenti",r/list.size());
		mapEngin.put("arret",a/list.size());

		Map<String, String> map2 = new HashMap<>();
		map2.put("production", dateUtility.convertToDate(mapEngin.get("production")));
		map2.put("ralenti", dateUtility.convertToDate(mapEngin.get("ralenti")));
		map2.put("arret", dateUtility.convertToDate(mapEngin.get("arret")));
		return map2;
	}

	private Map<String, Integer> doWork(Engin engin,Date from, Date to){
		List<Donnee> donneeList = donneeRepo.findBetween(engin.getId(), from, to);
		return traitementService.calcule(donneeList, engin.getTemps(), engin.getIntervale(), engin.getSeuilP(), engin.getSeuilR());
	}
}
