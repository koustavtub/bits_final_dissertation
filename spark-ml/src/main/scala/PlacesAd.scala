import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory
object PlacesAd {
  private val log = LoggerFactory.getLogger("PlacesAd")
  def main(args: Array[String]): Unit = {
    val appName = Some("MySparkMLApp")
    val sparkSessionBuilder = SparkSession
      .builder()

    val sparkSession = {
      val master = args.toList.find(arg => arg.startsWith("master="))
      if (master.isDefined) {
        val passedMaster = master.get.substring(7)
        sparkSessionBuilder.appName(appName.getOrElse("MySparkApp")).master(passedMaster).getOrCreate
      } else
        sparkSessionBuilder.appName(appName.getOrElse("MySparkApp")).getOrCreate
    }

    val sparkContext = sparkSession.sparkContext

    println(s"Spark Context: $sparkContext and App Id is ${sparkContext.getConf.getAppId} ")


    val dataFrame = sparkSession.read.csv("src/main/resources/ny.csv")
    dataFrame.show(5)
    dataFrame.printSchema()
    dataFrame.cache()

    val kiosks = sparkSession.read.csv("src/main/resources/kiosks.csv").rdd
    kiosks.cache()
    println(kiosks.count())


    val dataList = dataFrame.rdd.collect()

    val plcRdd = dataFrame.rdd
    plcRdd.cache().setName("plcRdd")

    val xyz: RDD[WritablePlace] =
      kiosks.map(data => {
        val reportingPlace = new SimplePlaceLinked()

        reportingPlace.uid = data.get(0).toString
        reportingPlace.name = data.get(3).toString
        reportingPlace.cats = data.get(4).toString

        val lat = java.lang.Double.parseDouble(data.get(1).toString)
        val lon = java.lang.Double.parseDouble(data.get(2).toString)
        reportingPlace.lat = lat
        reportingPlace.lon = lon

        dataList.foreach(dt => {
          val dist = new DistanceCalculatorImpl().calculateDistanceInKilometer(Location(lat, lon), Location(java.lang.Double.parseDouble(dt.get(1).toString), java.lang.Double.parseDouble(dt.get(2).toString)))

          if (dist <= 0.75 && (!dt.get(0).toString.equals(data.get(0).toString))) {
            setCats(reportingPlace, dt.get(4).toString)
          }

        })

         //log.info(reportingPlace.toString)

        WritablePlace(reportingPlace.toString)
      })

    xyz.cache()
    xyz.count()

    sparkSession.createDataFrame(xyz).repartition(1).write.text("src/main/resources/kiosk.csv")

  }

  private def setCats(reportingPlace: SimplePlaceLinked, catId: String) = {

    catId match {
      case "100-1000-0000" => reportingPlace.restaurant = reportingPlace.restaurant + 1
      case "100-1000-0001" => reportingPlace.casual_dining = reportingPlace.casual_dining + 1
      case "100-1000-0002" => reportingPlace.fine_dining = reportingPlace.fine_dining + 1
      case "100-1000-0003" => reportingPlace.take_out_and_delivery_only = reportingPlace.take_out_and_delivery_only + 1
      case "100-1000-0004" => reportingPlace.food_market_stall = reportingPlace.food_market_stall + 1
      case "100-1000-0005" => reportingPlace.taqueria = reportingPlace.taqueria + 1
      case "100-1000-0006" => reportingPlace.deli = reportingPlace.deli + 1
      case "100-1000-0007" => reportingPlace.cafeteria = reportingPlace.cafeteria + 1
      case "100-1000-0008" => reportingPlace.bistro = reportingPlace.bistro + 1
      case "100-1000-0009" => reportingPlace.fast_food = reportingPlace.fast_food + 1
      case "100-1100-0000" => reportingPlace.coffee_tea = reportingPlace.coffee_tea + 1
      case "100-1100-0010" => reportingPlace.coffee_shop = reportingPlace.coffee_shop + 1
      case "100-1100-0331" => reportingPlace.tea_house = reportingPlace.tea_house + 1
      case "200-2000-0000" => reportingPlace.nightlife_entertainment = reportingPlace.nightlife_entertainment + 1
      case "200-2000-0011" => reportingPlace.bar_or_pub = reportingPlace.bar_or_pub + 1
      case "200-2000-0012" => reportingPlace.night_club = reportingPlace.night_club + 1
      case "200-2000-0013" => reportingPlace.dancing = reportingPlace.dancing + 1
      case "200-2000-0014" => reportingPlace.karaoke = reportingPlace.karaoke + 1
      case "200-2000-0015" => reportingPlace.live_entertainment_music = reportingPlace.live_entertainment_music + 1
      case "200-2000-0016" => reportingPlace.billiards_pool_hall = reportingPlace.billiards_pool_hall + 1
      case "200-2000-0017" => reportingPlace.video_arcade_game_room = reportingPlace.video_arcade_game_room + 1
      case "200-2000-0018" => reportingPlace.jazz_club = reportingPlace.jazz_club + 1
      case "200-2000-0019" => reportingPlace.beer_garden = reportingPlace.beer_garden + 1
      case "200-2000-0306" => reportingPlace.adult_entertainment = reportingPlace.adult_entertainment + 1
      case "200-2000-0368" => reportingPlace.cocktail_lounge = reportingPlace.cocktail_lounge + 1
      case "200-2100-0019" => reportingPlace.cinema = reportingPlace.cinema + 1
      case "200-2200-0000" => reportingPlace.theatre_music_and_culture = reportingPlace.theatre_music_and_culture + 1
      case "200-2200-0020" => reportingPlace.performing_arts = reportingPlace.performing_arts + 1
      case "200-2300-0000" => reportingPlace.gambling_lottery_betting = reportingPlace.gambling_lottery_betting + 1
      case "200-2300-0021" => reportingPlace.casino = reportingPlace.casino + 1
      case "200-2300-0022" => reportingPlace.lottery_booth = reportingPlace.lottery_booth + 1
      case "300-3000-0000" => reportingPlace.landmark_attraction = reportingPlace.landmark_attraction + 1
      case "300-3000-0023" => reportingPlace.tourist_attraction = reportingPlace.tourist_attraction + 1
      case "300-3000-0024" => reportingPlace.gallery = reportingPlace.gallery + 1
      case "300-3000-0025" => reportingPlace.historical_monument = reportingPlace.historical_monument + 1
      case "300-3000-0065" => reportingPlace.winery = reportingPlace.winery + 1
      case "300-3000-0232" => reportingPlace.named_intersection_chowk = reportingPlace.named_intersection_chowk + 1
      case "300-3000-0350" => reportingPlace.brewery = reportingPlace.brewery + 1
      case "300-3000-0351" => reportingPlace.distillery = reportingPlace.distillery + 1
      case "300-3100-0000" => reportingPlace.museum = reportingPlace.museum + 1
      case "300-3100-0026" => reportingPlace.science_museum = reportingPlace.science_museum + 1
      case "300-3100-0027" => reportingPlace.childrens_museum = reportingPlace.childrens_museum + 1
      case "300-3100-0028" => reportingPlace.history_museum = reportingPlace.history_museum + 1
      case "300-3100-0029" => reportingPlace.art_museum = reportingPlace.art_museum + 1
      case "300-3200-0000" => reportingPlace.religious_place = reportingPlace.religious_place + 1
      case "300-3200-0030" => reportingPlace.church = reportingPlace.church + 1
      case "300-3200-0031" => reportingPlace.temple = reportingPlace.temple + 1
      case "300-3200-0032" => reportingPlace.synagogue = reportingPlace.synagogue + 1
      case "300-3200-0033" => reportingPlace.ashram = reportingPlace.ashram + 1
      case "300-3200-0034" => reportingPlace.mosque = reportingPlace.mosque + 1
      case "300-3200-0309" => reportingPlace.other_place_of_worship = reportingPlace.other_place_of_worship + 1
      case "300-3200-0375" => reportingPlace.gurdwara = reportingPlace.gurdwara + 1
      case "350-3500-0233" => reportingPlace.body_of_water = reportingPlace.body_of_water + 1
      case "350-3500-0234" => reportingPlace.reservoir = reportingPlace.reservoir + 1
      case "350-3500-0235" => reportingPlace.waterfall = reportingPlace.waterfall + 1
      case "350-3500-0300" => reportingPlace.bay_harbor = reportingPlace.bay_harbor + 1
      case "350-3500-0302" => reportingPlace.river = reportingPlace.river + 1
      case "350-3500-0303" => reportingPlace.canal = reportingPlace.canal + 1
      case "350-3500-0304" => reportingPlace.lake = reportingPlace.lake + 1
      case "350-3510-0236" => reportingPlace.mountain_or_hill = reportingPlace.mountain_or_hill + 1
      case "350-3510-0237" => reportingPlace.mountain_passes = reportingPlace.mountain_passes + 1
      case "350-3510-0238" => reportingPlace.mountain_peaks = reportingPlace.mountain_peaks + 1
      case "350-3520-0224" => reportingPlace.undersea_feature = reportingPlace.undersea_feature + 1
      case "350-3522-0239" => reportingPlace.forest_heath_or_other_vegetation = reportingPlace.forest_heath_or_other_vegetation + 1
      case "350-3550-0336" => reportingPlace.natural_and_geographical = reportingPlace.natural_and_geographical + 1
      case "400-4000-4580" => reportingPlace.public_sports_airport = reportingPlace.public_sports_airport + 1
      case "400-4000-4581" => reportingPlace.airport = reportingPlace.airport + 1
      case "400-4000-4582" => reportingPlace.airport_terminal = reportingPlace.airport_terminal + 1
      case "400-4100-0035" => reportingPlace.train_station = reportingPlace.train_station + 1
      case "400-4100-0036" => reportingPlace.bus_station = reportingPlace.bus_station + 1
      case "400-4100-0037" => reportingPlace.underground_train_subway = reportingPlace.underground_train_subway + 1
      case "400-4100-0038" => reportingPlace.commuter_rail_station = reportingPlace.commuter_rail_station + 1
      case "400-4100-0039" => reportingPlace.commuter_train = reportingPlace.commuter_train + 1
      case "400-4100-0040" => reportingPlace.public_transit_access = reportingPlace.public_transit_access + 1
      case "400-4100-0041" => reportingPlace.transportation_service = reportingPlace.transportation_service + 1
      case "400-4100-0042" => reportingPlace.bus_stop = reportingPlace.bus_stop + 1
      case "400-4100-0043" => reportingPlace.local_transit = reportingPlace.local_transit + 1
      case "400-4100-0044" => reportingPlace.ferry_terminal = reportingPlace.ferry_terminal + 1
      case "400-4100-0045" => reportingPlace.boat_ferry = reportingPlace.boat_ferry + 1
      case "400-4100-0046" => reportingPlace.rail_ferry = reportingPlace.rail_ferry + 1
      case "400-4100-0047" => reportingPlace.taxi_stand = reportingPlace.taxi_stand + 1
      case "400-4100-0226" => reportingPlace.highway_exit = reportingPlace.highway_exit + 1
      case "400-4100-0326" => reportingPlace.tollbooth = reportingPlace.tollbooth + 1
      case "400-4100-0337" => reportingPlace.lightrail = reportingPlace.lightrail + 1
      case "400-4100-0338" => reportingPlace.water_transit = reportingPlace.water_transit + 1
      case "400-4100-0339" => reportingPlace.monorail = reportingPlace.monorail + 1
      case "400-4100-0340" => reportingPlace.aerial_tramway = reportingPlace.aerial_tramway + 1
      case "400-4100-0341" => reportingPlace.bus_rapid_transit = reportingPlace.bus_rapid_transit + 1
      case "400-4100-0342" => reportingPlace.inclined_rail = reportingPlace.inclined_rail + 1
      case "400-4100-0347" => reportingPlace.bicycle_sharing_location = reportingPlace.bicycle_sharing_location + 1
      case "400-4100-0348" => reportingPlace.bicycle_parking = reportingPlace.bicycle_parking + 1
      case "400-4200-0048" => reportingPlace.weigh_station = reportingPlace.weigh_station + 1
      case "400-4200-0049" => reportingPlace.cargo_center = reportingPlace.cargo_center + 1
      case "400-4200-0050" => reportingPlace.rail_yard = reportingPlace.rail_yard + 1
      case "400-4200-0051" => reportingPlace.seaport_harbour = reportingPlace.seaport_harbour + 1
      case "400-4200-0052" => reportingPlace.airport_cargo = reportingPlace.airport_cargo + 1
      case "400-4200-0240" => reportingPlace.couriers = reportingPlace.couriers + 1
      case "400-4200-0241" => reportingPlace.cargo_transportation = reportingPlace.cargo_transportation + 1
      case "400-4200-0311" => reportingPlace.delivery_entrance = reportingPlace.delivery_entrance + 1
      case "400-4200-0312" => reportingPlace.loading_dock = reportingPlace.loading_dock + 1
      case "400-4200-0313" => reportingPlace.loading_zone = reportingPlace.loading_zone + 1
      case "400-4300-0000" => reportingPlace.rest_area = reportingPlace.rest_area + 1
      case "400-4300-0199" => reportingPlace.complete_rest_area = reportingPlace.complete_rest_area + 1
      case "400-4300-0200" => reportingPlace.parking_and_restroom_only_rest_area = reportingPlace.parking_and_restroom_only_rest_area + 1
      case "400-4300-0201" => reportingPlace.parking_only_rest_area = reportingPlace.parking_only_rest_area + 1
      case "400-4300-0202" => reportingPlace.motorway_service_rest_area = reportingPlace.motorway_service_rest_area + 1
      case "400-4300-0308" => reportingPlace.scenic_overlook_rest_area = reportingPlace.scenic_overlook_rest_area + 1
      case "500-5000-0000" => reportingPlace.hotel_or_motel = reportingPlace.hotel_or_motel + 1
      case "500-5000-0053" => reportingPlace.hotel = reportingPlace.hotel + 1
      case "500-5000-0054" => reportingPlace.motel = reportingPlace.motel + 1
      case "500-5100-0000" => reportingPlace.lodging = reportingPlace.lodging + 1
      case "500-5100-0055" => reportingPlace.hostel = reportingPlace.hostel + 1
      case "500-5100-0056" => reportingPlace.campground = reportingPlace.campground + 1
      case "500-5100-0057" => reportingPlace.guest_house = reportingPlace.guest_house + 1
      case "500-5100-0058" => reportingPlace.bed_and_breakfast = reportingPlace.bed_and_breakfast + 1
      case "500-5100-0059" => reportingPlace.holiday_park = reportingPlace.holiday_park + 1
      case "500-5100-0060" => reportingPlace.short_time_motel = reportingPlace.short_time_motel + 1
      case "550-5510-0000" => reportingPlace.outdoor_recreation = reportingPlace.outdoor_recreation + 1
      case "550-5510-0202" => reportingPlace.park_recreation_area = reportingPlace.park_recreation_area + 1
      case "550-5510-0203" => reportingPlace.sports_field = reportingPlace.sports_field + 1
      case "550-5510-0204" => reportingPlace.garden = reportingPlace.garden + 1
      case "550-5510-0205" => reportingPlace.beach = reportingPlace.beach + 1
      case "550-5510-0206" => reportingPlace.recreation_center = reportingPlace.recreation_center + 1
      case "550-5510-0227" => reportingPlace.ski_lift = reportingPlace.ski_lift + 1
      case "550-5510-0242" => reportingPlace.scenic_point = reportingPlace.scenic_point + 1
      case "550-5510-0358" => reportingPlace.off_road_trailhead = reportingPlace.off_road_trailhead + 1
      case "550-5510-0359" => reportingPlace.trailhead = reportingPlace.trailhead + 1
      case "550-5510-0374" => reportingPlace.off_road_vehicle_area = reportingPlace.off_road_vehicle_area + 1
      case "550-5510-0378" => reportingPlace.campsite = reportingPlace.campsite + 1
      case "550-5510-0379" => reportingPlace.outdoor_service = reportingPlace.outdoor_service + 1
      case "550-5510-0380" => reportingPlace.ranger_station = reportingPlace.ranger_station + 1
      case "550-5510-0387" => reportingPlace.bicycle_service = reportingPlace.bicycle_service + 1
      case "550-5520-0000" => reportingPlace.leisure = reportingPlace.leisure + 1
      case "550-5520-0207" => reportingPlace.amusement_park = reportingPlace.amusement_park + 1
      case "550-5520-0208" => reportingPlace.zoo = reportingPlace.zoo + 1
      case "550-5520-0209" => reportingPlace.wild_animal_park = reportingPlace.wild_animal_park + 1
      case "550-5520-0210" => reportingPlace.wildlife_refuge = reportingPlace.wildlife_refuge + 1
      case "550-5520-0211" => reportingPlace.aquarium = reportingPlace.aquarium + 1
      case "550-5520-0212" => reportingPlace.ski_resort = reportingPlace.ski_resort + 1
      case "550-5520-0228" => reportingPlace.animal_park = reportingPlace.animal_park + 1
      case "550-5520-0357" => reportingPlace.water_park = reportingPlace.water_park + 1
      case "600-6000-0061" => reportingPlace.convenience_store = reportingPlace.convenience_store + 1
      case "600-6100-0062" => reportingPlace.shopping_mall = reportingPlace.shopping_mall + 1
      case "600-6200-0063" => reportingPlace.department_store = reportingPlace.department_store + 1
      case "600-6300-0064" => reportingPlace.food_beverage_specialty_store = reportingPlace.food_beverage_specialty_store + 1
      case "600-6300-0066" => reportingPlace.grocery = reportingPlace.grocery + 1
      case "600-6300-0067" => reportingPlace.specialty_food_store = reportingPlace.specialty_food_store + 1
      case "600-6300-0068" => reportingPlace.wine_and_liquor = reportingPlace.wine_and_liquor + 1
      case "600-6300-0244" => reportingPlace.bakery_and_baked_goods_store = reportingPlace.bakery_and_baked_goods_store + 1
      case "600-6300-0246" => reportingPlace.doughnut_shop = reportingPlace.doughnut_shop + 1
      case "600-6300-0245" => reportingPlace.sweet_shop = reportingPlace.sweet_shop + 1
      case "600-6300-0363" => reportingPlace.butcher = reportingPlace.butcher + 1
      case "600-6300-0364" => reportingPlace.dairy_goods = reportingPlace.dairy_goods + 1
      case "600-6400-0000" => reportingPlace.drugstore_or_pharmacy = reportingPlace.drugstore_or_pharmacy + 1
      case "600-6400-0069" => reportingPlace.drugstore = reportingPlace.drugstore + 1
      case "600-6400-0070" => reportingPlace.pharmacy = reportingPlace.pharmacy + 1
      case "600-6500-0072" => reportingPlace.consumer_electronics_store = reportingPlace.consumer_electronics_store + 1
      case "600-6500-0073" => reportingPlace.mobile_retailer = reportingPlace.mobile_retailer + 1
      case "600-6500-0074" => reportingPlace.mobile_service_center = reportingPlace.mobile_service_center + 1
      case "600-6500-0075" => reportingPlace.computer_and_software = reportingPlace.computer_and_software + 1
      case "600-6500-0076" => reportingPlace.entertainment_electronics = reportingPlace.entertainment_electronics + 1
      case "600-6600-0000" => reportingPlace.hardware_house_and_garden = reportingPlace.hardware_house_and_garden + 1
      case "600-6600-0077" => reportingPlace.home_improvement_hardware_store = reportingPlace.home_improvement_hardware_store + 1
      case "600-6600-0078" => reportingPlace.home_specialty_store = reportingPlace.home_specialty_store + 1
      case "600-6600-0079" => reportingPlace.floor_and_carpet = reportingPlace.floor_and_carpet + 1
      case "600-6600-0080" => reportingPlace.furniture_store = reportingPlace.furniture_store + 1
      case "600-6600-0082" => reportingPlace.garden_center = reportingPlace.garden_center + 1
      case "600-6600-0083" => reportingPlace.glass_and_window = reportingPlace.glass_and_window + 1
      case "600-6600-0084" => reportingPlace.lumber = reportingPlace.lumber + 1
      case "600-6600-0085" => reportingPlace.major_appliance = reportingPlace.major_appliance + 1
      case "600-6600-0310" => reportingPlace.power_equipment_dealer = reportingPlace.power_equipment_dealer + 1
      case "600-6600-0319" => reportingPlace.paint_store = reportingPlace.paint_store + 1
      case "600-6700-0000" => reportingPlace.other_bookshop = reportingPlace.other_bookshop + 1
      case "600-6700-0087" => reportingPlace.bookstore = reportingPlace.bookstore + 1
      case "600-6800-0000" => reportingPlace.clothing_and_accessories = reportingPlace.clothing_and_accessories + 1
      case "600-6800-0089" => reportingPlace.mens_apparel = reportingPlace.mens_apparel + 1
      case "600-6800-0090" => reportingPlace.womens_apparel = reportingPlace.womens_apparel + 1
      case "600-6800-0091" => reportingPlace.childrens_apparel = reportingPlace.childrens_apparel + 1
      case "600-6800-0092" => reportingPlace.shoes_footwear = reportingPlace.shoes_footwear + 1
      case "600-6800-0093" => reportingPlace.specialty_clothing_store = reportingPlace.specialty_clothing_store + 1
      case "600-6900-0000" => reportingPlace.consumer_goods = reportingPlace.consumer_goods + 1
      case "600-6900-0094" => reportingPlace.sporting_goods_store = reportingPlace.sporting_goods_store + 1
      case "600-6900-0095" => reportingPlace.office_supply_and_services_store = reportingPlace.office_supply_and_services_store + 1
      case "600-6900-0096" => reportingPlace.specialty_store = reportingPlace.specialty_store + 1
      case "600-6900-0097" => reportingPlace.pet_supply = reportingPlace.pet_supply + 1
      case "600-6900-0098" => reportingPlace.warehouse_wholesale_store = reportingPlace.warehouse_wholesale_store + 1
      case "600-6900-0099" => reportingPlace.general_merchandise = reportingPlace.general_merchandise + 1
      case "600-6900-0100" => reportingPlace.discount_store = reportingPlace.discount_store + 1
      case "600-6900-0101" => reportingPlace.flowers_and_jewelry = reportingPlace.flowers_and_jewelry + 1
      case "600-6900-0102" => reportingPlace.variety_store = reportingPlace.variety_store + 1
      case "600-6900-0103" => reportingPlace.gift_antique_and_art = reportingPlace.gift_antique_and_art + 1
      case "600-6900-0105" => reportingPlace.record_cd_and_video = reportingPlace.record_cd_and_video + 1
      case "600-6900-0106" => reportingPlace.video_and_game_rental = reportingPlace.video_and_game_rental + 1
      case "600-6900-0107" => reportingPlace.cigar_and_tobacco = reportingPlace.cigar_and_tobacco + 1
      case "600-6900-0108" => reportingPlace.vaping_store = reportingPlace.vaping_store + 1
      case "600-6900-0246" => reportingPlace.bicycle_and_bicycle_accessories_shop = reportingPlace.bicycle_and_bicycle_accessories_shop + 1
      case "600-6900-0247" => reportingPlace.market = reportingPlace.market + 1
      case "600-6900-0248" => reportingPlace.motorcycle_accessories = reportingPlace.motorcycle_accessories + 1
      case "600-6900-0249" => reportingPlace.non_store_retailers = reportingPlace.non_store_retailers + 1
      case "600-6900-0250" => reportingPlace.pawnshop = reportingPlace.pawnshop + 1
      case "600-6900-0251" => reportingPlace.used_second_hand_merchandise_stores = reportingPlace.used_second_hand_merchandise_stores + 1
      case "600-6900-0305" => reportingPlace.adult_shop = reportingPlace.adult_shop + 1
      case "600-6900-0307" => reportingPlace.arts_and_crafts_supplies = reportingPlace.arts_and_crafts_supplies + 1
      case "600-6900-0355" => reportingPlace.florist = reportingPlace.florist + 1
      case "600-6900-0356" => reportingPlace.jeweler = reportingPlace.jeweler + 1
      case "600-6900-0358" => reportingPlace.toy_store = reportingPlace.toy_store + 1
      case "600-6900-0388" => reportingPlace.hunting_fishing_shop = reportingPlace.hunting_fishing_shop + 1
      case "600-6900-0389" => reportingPlace.running_walking_shop = reportingPlace.running_walking_shop + 1
      case "600-6900-0390" => reportingPlace.skate_shop = reportingPlace.skate_shop + 1
      case "600-6900-0391" => reportingPlace.ski_shop = reportingPlace.ski_shop + 1
      case "600-6900-0392" => reportingPlace.snowboard_shop = reportingPlace.snowboard_shop + 1
      case "600-6900-0393" => reportingPlace.surf_shop = reportingPlace.surf_shop + 1
      case "600-6900-0394" => reportingPlace.bmx_shop = reportingPlace.bmx_shop + 1
      case "600-6900-0395" => reportingPlace.camping_hiking_shop = reportingPlace.camping_hiking_shop + 1
      case "600-6900-0396" => reportingPlace.canoe_kayak_shop = reportingPlace.canoe_kayak_shop + 1
      case "600-6900-0397" => reportingPlace.cross_country_ski_shop = reportingPlace.cross_country_ski_shop + 1
      case "600-6900-0398" => reportingPlace.tack_shop = reportingPlace.tack_shop + 1
      case "600-6950-0000" => reportingPlace.hair_and_beauty = reportingPlace.hair_and_beauty + 1
      case "600-6950-0399" => reportingPlace.barber = reportingPlace.barber + 1
      case "600-6950-0400" => reportingPlace.nail_salon = reportingPlace.nail_salon + 1
      case "600-6950-0401" => reportingPlace.hair_salon = reportingPlace.hair_salon + 1
      case "700-7000-0107" => reportingPlace.bank = reportingPlace.bank + 1
      case "700-7010-0108" => reportingPlace.atm = reportingPlace.atm + 1
      case "700-7050-0109" => reportingPlace.money_transferring_service = reportingPlace.money_transferring_service + 1
      case "700-7050-0110" => reportingPlace.check_cashing_service_currency_exchange = reportingPlace.check_cashing_service_currency_exchange + 1
      case "700-7100-0000" => reportingPlace.communication_media = reportingPlace.communication_media + 1
      case "700-7100-0134" => reportingPlace.telephone_service = reportingPlace.telephone_service + 1
      case "700-7200-0000" => reportingPlace.commercial_services = reportingPlace.commercial_services + 1
      case "700-7200-0252" => reportingPlace.advertising_marketing_pr_and_market_research = reportingPlace.advertising_marketing_pr_and_market_research + 1
      case "700-7200-0253" => reportingPlace.catering_and_other_food_services = reportingPlace.catering_and_other_food_services + 1
      case "700-7200-0254" => reportingPlace.construction = reportingPlace.construction + 1
      case "700-7200-0255" => reportingPlace.customer_care_service_center = reportingPlace.customer_care_service_center + 1
      case "700-7200-0256" => reportingPlace.engineering_and_scientific_services = reportingPlace.engineering_and_scientific_services + 1
      case "700-7200-0257" => reportingPlace.farming = reportingPlace.farming + 1
      case "700-7200-0258" => reportingPlace.food_production = reportingPlace.food_production + 1
      case "700-7200-0259" => reportingPlace.human_resources_and_recruiting_services = reportingPlace.human_resources_and_recruiting_services + 1
      case "700-7200-0260" => reportingPlace.investigation_services = reportingPlace.investigation_services + 1
      case "700-7200-0261" => reportingPlace.it_and_office_equipment_services = reportingPlace.it_and_office_equipment_services + 1
      case "700-7200-0262" => reportingPlace.landscaping_services = reportingPlace.landscaping_services + 1
      case "700-7200-0263" => reportingPlace.locksmiths_and_security_systems_services = reportingPlace.locksmiths_and_security_systems_services + 1
      case "700-7200-0264" => reportingPlace.management_and_consulting_services = reportingPlace.management_and_consulting_services + 1
      case "700-7200-0265" => reportingPlace.manufacturing = reportingPlace.manufacturing + 1
      case "700-7200-0266" => reportingPlace.mining = reportingPlace.mining + 1
      case "700-7200-0267" => reportingPlace.modeling_agencies = reportingPlace.modeling_agencies + 1
      case "700-7200-0268" => reportingPlace.motorcycle_service_and_maintenance = reportingPlace.motorcycle_service_and_maintenance + 1
      case "700-7200-0269" => reportingPlace.organizations_and_societies = reportingPlace.organizations_and_societies + 1
      case "700-7200-0270" => reportingPlace.entertainment_and_recreation = reportingPlace.entertainment_and_recreation + 1
      case "700-7200-0271" => reportingPlace.finance_and_insurance = reportingPlace.finance_and_insurance + 1
      case "700-7200-0272" => reportingPlace.healthcare_and_healthcare_support_services = reportingPlace.healthcare_and_healthcare_support_services + 1
      case "700-7200-0274" => reportingPlace.rental_and_leasing = reportingPlace.rental_and_leasing + 1
      case "700-7200-0275" => reportingPlace.repair_and_maintenance_services = reportingPlace.repair_and_maintenance_services + 1
      case "700-7200-0276" => reportingPlace.printing_and_publishing = reportingPlace.printing_and_publishing + 1
      case "700-7200-0277" => reportingPlace.specialty_trade_contractors = reportingPlace.specialty_trade_contractors + 1
      case "700-7200-0278" => reportingPlace.towing_service = reportingPlace.towing_service + 1
      case "700-7200-0279" => reportingPlace.translation_and_interpretation_services = reportingPlace.translation_and_interpretation_services + 1
      case "700-7200-0324" => reportingPlace.apartment_rental_flat_rental = reportingPlace.apartment_rental_flat_rental + 1
      case "700-7200-0328" => reportingPlace.b2b_sales_and_services = reportingPlace.b2b_sales_and_services + 1
      case "700-7200-0329" => reportingPlace.b2b_restaurant_services = reportingPlace.b2b_restaurant_services + 1
      case "700-7200-0330" => reportingPlace.aviation = reportingPlace.aviation + 1
      case "700-7200-0342" => reportingPlace.interior_and_exterior_design = reportingPlace.interior_and_exterior_design + 1
      case "700-7200-0344" => reportingPlace.property_management = reportingPlace.property_management + 1
      case "700-7200-0345" => reportingPlace.financial_investment_firm = reportingPlace.financial_investment_firm + 1
      case "700-7250-0136" => reportingPlace.business_facility = reportingPlace.business_facility + 1
      case "700-7300-0111" => reportingPlace.police_station = reportingPlace.police_station + 1
      case "700-7300-0112" => reportingPlace.police_services_security = reportingPlace.police_services_security + 1
      case "700-7300-0113" => reportingPlace.fire_department = reportingPlace.fire_department + 1
      case "700-7300-0280" => reportingPlace.ambulance_services = reportingPlace.ambulance_services + 1
      case "700-7400-0000" => reportingPlace.consumer_services = reportingPlace.consumer_services + 1
      case "700-7400-0133" => reportingPlace.travel_agent_ticketing = reportingPlace.travel_agent_ticketing + 1
      case "700-7400-0137" => reportingPlace.dry_cleaning_and_laundry = reportingPlace.dry_cleaning_and_laundry + 1
      case "700-7400-0138" => reportingPlace.attorney = reportingPlace.attorney + 1
      case "700-7400-0140" => reportingPlace.boating = reportingPlace.boating + 1
      case "700-7400-0141" => reportingPlace.business_service = reportingPlace.business_service + 1
      case "700-7400-0142" => reportingPlace.funeral_director = reportingPlace.funeral_director + 1
      case "700-7400-0143" => reportingPlace.mover = reportingPlace.mover + 1
      case "700-7400-0144" => reportingPlace.photography = reportingPlace.photography + 1
      case "700-7400-0145" => reportingPlace.real_estate_services = reportingPlace.real_estate_services + 1
      case "700-7400-0146" => reportingPlace.repair_service = reportingPlace.repair_service + 1
      case "700-7400-0147" => reportingPlace.social_service = reportingPlace.social_service + 1
      case "700-7400-0148" => reportingPlace.storage = reportingPlace.storage + 1
      case "700-7400-0149" => reportingPlace.tailor_and_alteration = reportingPlace.tailor_and_alteration + 1
      case "700-7400-0150" => reportingPlace.tax_service = reportingPlace.tax_service + 1
      case "700-7400-0151" => reportingPlace.utilities = reportingPlace.utilities + 1
      case "700-7400-0152" => reportingPlace.waste_and_sanitary = reportingPlace.waste_and_sanitary + 1
      case "700-7400-0281" => reportingPlace.bicycle_service_and_maintenance = reportingPlace.bicycle_service_and_maintenance + 1
      case "700-7400-0282" => reportingPlace.bill_payment_service = reportingPlace.bill_payment_service + 1
      case "700-7400-0283" => reportingPlace.body_piercing_and_tattoos = reportingPlace.body_piercing_and_tattoos + 1
      case "700-7400-0284" => reportingPlace.wedding_services_and_bridal_studio = reportingPlace.wedding_services_and_bridal_studio + 1
      case "700-7400-0285" => reportingPlace.internet_cafe = reportingPlace.internet_cafe + 1
      case "700-7400-0286" => reportingPlace.kindergarten_and_childcare = reportingPlace.kindergarten_and_childcare + 1
      case "700-7400-0287" => reportingPlace.maid_services = reportingPlace.maid_services + 1
      case "700-7400-0288" => reportingPlace.marriage_and_match_making_services = reportingPlace.marriage_and_match_making_services + 1
      case "700-7400-0289" => reportingPlace.public_administration = reportingPlace.public_administration + 1
      case "700-7400-0292" => reportingPlace.wellness_center_and_services = reportingPlace.wellness_center_and_services + 1
      case "700-7400-0293" => reportingPlace.pet_care = reportingPlace.pet_care + 1
      case "700-7400-0327" => reportingPlace.legal_services = reportingPlace.legal_services + 1
      case "700-7400-0343" => reportingPlace.tanning_salon = reportingPlace.tanning_salon + 1
      case "700-7400-0352" => reportingPlace.recycling_center = reportingPlace.recycling_center + 1
      case "700-7400-0365" => reportingPlace.electrical = reportingPlace.electrical + 1
      case "700-7400-0366" => reportingPlace.plumbing = reportingPlace.plumbing + 1
      case "700-7450-0114" => reportingPlace.post_office = reportingPlace.post_office + 1
      case "700-7460-0115" => reportingPlace.tourist_information = reportingPlace.tourist_information + 1
      case "700-7600-0000" => reportingPlace.fueling_station = reportingPlace.fueling_station + 1
      case "700-7600-0116" => reportingPlace.petrol_gasoline_station = reportingPlace.petrol_gasoline_station + 1
      case "700-7600-0322" => reportingPlace.ev_charging_station = reportingPlace.ev_charging_station + 1
      case "700-7800-0118" => reportingPlace.automobile_dealership_new_cars = reportingPlace.automobile_dealership_new_cars + 1
      case "700-7800-0119" => reportingPlace.automobile_dealership_used_cars = reportingPlace.automobile_dealership_used_cars + 1
      case "700-7800-0120" => reportingPlace.motorcycle_dealership = reportingPlace.motorcycle_dealership + 1
      case "700-7850-0000" => reportingPlace.car_repair_service = reportingPlace.car_repair_service + 1
      case "700-7850-0121" => reportingPlace.car_wash_detailing = reportingPlace.car_wash_detailing + 1
      case "700-7850-0122" => reportingPlace.car_repair = reportingPlace.car_repair + 1
      case "700-7850-0123" => reportingPlace.auto_parts = reportingPlace.auto_parts + 1
      case "700-7850-0124" => reportingPlace.emission_testing = reportingPlace.emission_testing + 1
      case "700-7850-0125" => reportingPlace.tire_repair = reportingPlace.tire_repair + 1
      case "700-7850-0126" => reportingPlace.truck_repair = reportingPlace.truck_repair + 1
      case "700-7850-0127" => reportingPlace.van_repair = reportingPlace.van_repair + 1
      case "700-7850-0128" => reportingPlace.road_assistance = reportingPlace.road_assistance + 1
      case "700-7850-0129" => reportingPlace.automobile_club = reportingPlace.automobile_club + 1
      case "700-7851-0117" => reportingPlace.rental_car_agency = reportingPlace.rental_car_agency + 1
      case "700-7900-0000" => reportingPlace.truck_semi_dealer_services = reportingPlace.truck_semi_dealer_services + 1
      case "700-7900-0130" => reportingPlace.truck_dealership = reportingPlace.truck_dealership + 1
      case "700-7900-0131" => reportingPlace.truck_parking = reportingPlace.truck_parking + 1
      case "700-7900-0132" => reportingPlace.truck_stop_plaza = reportingPlace.truck_stop_plaza + 1
      case "700-7900-0323" => reportingPlace.truck_wash = reportingPlace.truck_wash + 1
      case "800-8000-0000" => reportingPlace.hospital_or_health_care_facility = reportingPlace.hospital_or_health_care_facility + 1
      case "800-8000-0154" => reportingPlace.dentist_dental_office = reportingPlace.dentist_dental_office + 1
      case "800-8000-0155" => reportingPlace.family_general_practice_physicians = reportingPlace.family_general_practice_physicians + 1
      case "800-8000-0156" => reportingPlace.psychiatric_institute = reportingPlace.psychiatric_institute + 1
      case "800-8000-0157" => reportingPlace.nursing_home = reportingPlace.nursing_home + 1
      case "800-8000-0158" => reportingPlace.medical_services_clinics = reportingPlace.medical_services_clinics + 1
      case "800-8000-0159" => reportingPlace.hospital = reportingPlace.hospital + 1
      case "800-8000-0161" => reportingPlace.optical = reportingPlace.optical + 1
      case "800-8000-0162" => reportingPlace.veterinarian = reportingPlace.veterinarian + 1
      case "800-8000-0325" => reportingPlace.hospital_emergency_room = reportingPlace.hospital_emergency_room + 1
      case "800-8000-0340" => reportingPlace.therapist = reportingPlace.therapist + 1
      case "800-8000-0341" => reportingPlace.chiropractor = reportingPlace.chiropractor + 1
      case "800-8000-0367" => reportingPlace.blood_bank = reportingPlace.blood_bank + 1
      case "800-8000-0400" => reportingPlace.covid_testing_site = reportingPlace.covid_testing_site + 1
      case "800-8100-0000" => reportingPlace.government_or_community_facility = reportingPlace.government_or_community_facility + 1
      case "800-8100-0163" => reportingPlace.city_hall = reportingPlace.city_hall + 1
      case "800-8100-0164" => reportingPlace.embassy = reportingPlace.embassy + 1
      case "800-8100-0165" => reportingPlace.military_base = reportingPlace.military_base + 1
      case "800-8100-0168" => reportingPlace.county_council = reportingPlace.county_council + 1
      case "800-8100-0169" => reportingPlace.civic_community_center = reportingPlace.civic_community_center + 1
      case "800-8100-0170" => reportingPlace.court_house = reportingPlace.court_house + 1
      case "800-8100-0171" => reportingPlace.government_office = reportingPlace.government_office + 1
      case "800-8100-0172" => reportingPlace.border_crossing = reportingPlace.border_crossing + 1
      case "800-8200-0000" => reportingPlace.education_facility = reportingPlace.education_facility + 1
      case "800-8200-0173" => reportingPlace.higher_education = reportingPlace.higher_education + 1
      case "800-8200-0174" => reportingPlace.school = reportingPlace.school + 1
      case "800-8200-0295" => reportingPlace.training_and_development = reportingPlace.training_and_development + 1
      case "800-8200-0360" => reportingPlace.coaching_institute = reportingPlace.coaching_institute + 1
      case "800-8200-0361" => reportingPlace.fine_arts = reportingPlace.fine_arts + 1
      case "800-8200-0362" => reportingPlace.language_studies = reportingPlace.language_studies + 1
      case "800-8250-0287" => reportingPlace.elementary_school = reportingPlace.elementary_school + 1
      case "800-8250-0288" => reportingPlace.secondary_school = reportingPlace.secondary_school + 1
      case "800-8300-0000" => reportingPlace.other_library = reportingPlace.other_library + 1
      case "800-8300-0175" => reportingPlace.library = reportingPlace.library + 1
      case "800-8400-0000" => reportingPlace.event_spaces = reportingPlace.event_spaces + 1
      case "800-8400-0139" => reportingPlace.banquet_hall = reportingPlace.banquet_hall + 1
      case "800-8400-0176" => reportingPlace.convention_exhibition_center = reportingPlace.convention_exhibition_center + 1
      case "800-8500-0000" => reportingPlace.parking = reportingPlace.parking + 1
      case "800-8500-0178" => reportingPlace.parking_lot = reportingPlace.parking_lot + 1
      case "800-8500-0179" => reportingPlace.park_and_ride = reportingPlace.park_and_ride + 1
      case "800-8600-0000" => reportingPlace.sports_facility_venue = reportingPlace.sports_facility_venue + 1
      case "800-8600-0180" => reportingPlace.sports_complex_stadium = reportingPlace.sports_complex_stadium + 1
      case "800-8600-0181" => reportingPlace.ice_skating_rink = reportingPlace.ice_skating_rink + 1
      case "800-8600-0182" => reportingPlace.swimming_pool = reportingPlace.swimming_pool + 1
      case "800-8600-0183" => reportingPlace.tennis_court = reportingPlace.tennis_court + 1
      case "800-8600-0184" => reportingPlace.bowling_center = reportingPlace.bowling_center + 1
      case "800-8600-0185" => reportingPlace.indoor_ski = reportingPlace.indoor_ski + 1
      case "800-8600-0186" => reportingPlace.hockey = reportingPlace.hockey + 1
      case "800-8600-0187" => reportingPlace.racquetball_court = reportingPlace.racquetball_court + 1
      case "800-8600-0188" => reportingPlace.shooting_range = reportingPlace.shooting_range + 1
      case "800-8600-0189" => reportingPlace.soccer_club = reportingPlace.soccer_club + 1
      case "800-8600-0190" => reportingPlace.squash_court = reportingPlace.squash_court + 1
      case "800-8600-0191" => reportingPlace.fitness_health_club = reportingPlace.fitness_health_club + 1
      case "800-8600-0192" => reportingPlace.indoor_sports = reportingPlace.indoor_sports + 1
      case "800-8600-0193" => reportingPlace.golf_course = reportingPlace.golf_course + 1
      case "800-8600-0194" => reportingPlace.golf_practice_range = reportingPlace.golf_practice_range + 1
      case "800-8600-0195" => reportingPlace.race_track = reportingPlace.race_track + 1
      case "800-8600-0196" => reportingPlace.sporting_instruction_and_camps = reportingPlace.sporting_instruction_and_camps + 1
      case "800-8600-0197" => reportingPlace.sports_activities = reportingPlace.sports_activities + 1
      case "800-8600-0199" => reportingPlace.basketball = reportingPlace.basketball + 1
      case "800-8600-0200" => reportingPlace.badminton = reportingPlace.badminton + 1
      case "800-8600-0314" => reportingPlace.rugby = reportingPlace.rugby + 1
      case "800-8600-0316" => reportingPlace.diving_center = reportingPlace.diving_center + 1
      case "800-8600-0376" => reportingPlace.bike_park = reportingPlace.bike_park + 1
      case "800-8700-0000" => reportingPlace.facilities = reportingPlace.facilities + 1
      case "800-8700-0166" => reportingPlace.cemetery = reportingPlace.cemetery + 1
      case "800-8700-0167" => reportingPlace.crematorium = reportingPlace.crematorium + 1
      case "800-8700-0198" => reportingPlace.public_restroom_toilets = reportingPlace.public_restroom_toilets + 1
      case "800-8700-0296" => reportingPlace.clubhouse = reportingPlace.clubhouse + 1
      case "800-8700-0298" => reportingPlace.registration_office = reportingPlace.registration_office + 1
      case "900-9200-0219" => reportingPlace.marina = reportingPlace.marina + 1
      case "900-9200-0220" => reportingPlace.rv_parks = reportingPlace.rv_parks + 1
      case "900-9200-0299" => reportingPlace.collective_community = reportingPlace.collective_community + 1
      case "900-9200-0301" => reportingPlace.island = reportingPlace.island + 1
      case "900-9200-0386" => reportingPlace.meeting_point = reportingPlace.meeting_point + 1
      case "900-9300-0000" => reportingPlace.building = reportingPlace.building + 1
      case "900-9300-0221" => reportingPlace.residential_area_building = reportingPlace.residential_area_building + 1
      case _:String=>//log.error("No cat mapping")
    }


  }
}
