package my.cardholder.util.logo

/**
 * Popular stores and services whose cards people keep, with the names they are typed by.
 * The first name is shown in suggestions. Every website here has an icon of at least 32px
 * (see [my.cardholder.util.LogoLoader]), or a link to a sharp icon of its own: sites with
 * only a blurry 16px icon are left out.
 */
object BrandCatalog {

    /** The [logo] is a website, whose icon is the logo, or a link to an image */
    data class Brand(val logo: String, val names: List<String>) {
        val displayName: String get() = names.first()
    }

    private fun brand(logo: String, vararg names: String) = Brand(logo, names.toList())

    val brands: List<Brand> = listOf(
        // Groceries
        brand("5ka.ru", "Пятёрочка", "Pyaterochka", "5ka"),
        brand("perekrestok.ru", "Перекрёсток", "Perekrestok"),
        brand("magnit.ru", "Магнит", "Magnit", "Магнит Косметик"),
        brand("lenta.com", "Лента", "Lenta"),
        brand("vkusvill.ru", "ВкусВилл", "VkusVill"),
        brand("auchan.ru", "Ашан", "Auchan"),
        brand("metro-cc.ru", "Metro", "Метро"),
        brand("okmarket.ru", "О'КЕЙ", "Окей", "OKEY"),
        brand("globus.ru", "Глобус", "Globus"),
        brand("spar-online.ru", "Spar", "Спар"),
        brand("av.ru", "Азбука вкуса", "Azbuka Vkusa"),
        brand("monetka.ru", "Монетка", "Monetka"),
        brand("maria-ra.ru", "Мария-Ра", "Maria-Ra"),
        brand("fix-price.com", "Fix Price", "Фикс Прайс"),
        brand("bristol.ru", "Бристоль", "Bristol"),
        brand("krasnoeibeloe.ru", "Красное&Белое", "Красное и Белое", "KB"),
        brand("chizhik.club", "Чижик", "Chizhik"),
        brand("samokat.ru", "Самокат", "Samokat"),
        brand("lavka.yandex.ru", "Яндекс Лавка", "Yandex Lavka"),
        brand("yarcheplus.ru", "Ярче", "Yarche"),
        brand("winelab.ru", "Винлаб", "WineLab"),
        brand("amwine.ru", "Ароматный мир", "Aromatny Mir", "AMWine"),
        brand("svetofore.ru", "Светофор", "Svetofor"),
        // Beauty
        brand("r-ulybka.ru", "Улыбка радуги", "Ulybka Radugi"),
        brand("goldapple.ru", "Золотое яблоко", "Gold Apple", "Zolotoe Yabloko"),
        brand("letu.ru", "Л'Этуаль", "Летуаль", "Letu", "L'Etoile"),
        brand("rivegauche.ru", "Рив Гош", "Rive Gauche"),
        brand("iledebeaute.ru", "Иль де Ботэ", "Il de Beaute", "Ile de Beaute"),
        // Pharmacies
        brand("rigla.ru", "Ригла", "Rigla"),
        brand("gorzdrav.org", "Горздрав", "Gorzdrav"),
        brand("366.ru", "36,6", "366"),
        brand("apteka.ru", "Аптека.ру", "Apteka.ru"),
        brand("eapteka.ru", "ЕАптека", "eApteka"),
        brand("zdravcity.ru", "Здравсити", "ZdravCity"),
        brand("planetazdorovo.ru", "Планета здоровья", "Planeta Zdorovya"),
        brand("vitaexpress.ru", "Вита", "Vita", "Вита Экспресс"),
        brand("apteka-april.ru", "Апрель", "April"),
        brand("asna.ru", "Асна", "ASNA"),
        brand("stolichki.ru", "Столички", "Stolichki"),
        brand("maksavit.ru", "Максавит", "Maksavit"),
        // Electronics and phones
        brand("mvideo.ru", "М.Видео", "MVideo", "Мвидео"),
        brand("eldorado.ru", "Эльдорадо", "Eldorado"),
        brand("dns-shop.ru", "DNS", "ДНС"),
        brand("www.citilink.ru", "Ситилинк", "Citilink"),
        brand("re-store.ru", "re:Store", "Рестор"),
        brand("mts.ru", "МТС", "MTS"),
        brand("beeline.ru", "Билайн", "Beeline"),
        brand("megafon.ru", "МегаФон", "MegaFon"),
        brand("t2.ru", "T2", "Tele2", "Т2", "Теле2"),
        // Home and DIY
        brand("lemanapro.ru", "Лемана ПРО", "Lemana PRO", "Леруа Мерлен", "Leroy Merlin"),
        brand("obi.ru", "OBI", "Оби"),
        brand("petrovich.ru", "Петрович", "Petrovich"),
        brand("maxidom.ru", "Максидом", "Maxidom"),
        brand("vseinstrumenti.ru", "ВсеИнструменты.ру", "Все инструменты", "VseInstrumenti"),
        brand("hoff.ru", "Hoff", "Хофф"),
        brand("galamart.ru", "Галамарт", "Galamart"),
        brand("askona.ru", "Аскона", "Askona"),
        // Marketplaces, children and books
        brand("ozon.ru", "Ozon", "Озон"),
        brand("wildberries.ru", "Wildberries", "Вайлдберриз", "WB"),
        brand("market.yandex.ru", "Яндекс Маркет", "Yandex Market"),
        brand("lamoda.ru", "Lamoda", "Ламода"),
        brand("detmir.ru", "Детский мир", "Detsky Mir", "Detmir"),
        brand("leonardo.ru", "Леонардо", "Leonardo"),
        brand("chitai-gorod.ru", "Читай-город", "Chitai Gorod"),
        brand("bookvoed.ru", "Буквоед", "Bookvoed"),
        brand("labirint.ru", "Лабиринт", "Labirint"),
        // Clothes, shoes and jewelry
        brand("sportmaster.ru", "Спортмастер", "Sportmaster"),
        brand("gloria-jeans.ru", "Gloria Jeans", "Глория Джинс"),
        brand("ostin.com", "O'STIN", "Остин", "Ostin"),
        brand("zarina.ru", "Zarina", "Зарина"),
        brand("befree.ru", "Befree", "Бифри"),
        brand("loverepublic.ru", "Love Republic", "Лав Репаблик"),
        brand("tvoe.ru", "Твоё", "Tvoe"),
        brand("famil.ru", "Familia", "Фамилия"),
        brand("kari.com", "Kari", "Кари"),
        brand("rendez-vous.ru", "Rendez-Vous", "Рандеву"),
        brand("zoloto585.ru", "585 Золотой", "585", "Zoloto 585"),
        brand("sunlight.net", "Sunlight", "Санлайт"),
        brand("sokolov.ru", "Sokolov", "Соколов"),
        // Pets
        brand("4lapy.ru", "Четыре лапы", "4 лапы", "Chetyre Lapy"),
        // Cafes and restaurants
        brand("coffeehouse.ru", "Кофе Хауз", "Coffee House"),
        brand("cofix.ru", "Cofix", "Кофикс"),
        brand("dodopizza.ru", "Додо Пицца", "Dodo Pizza"),
        brand("vkusnoitochka.ru", "Вкусно — и точка", "Vkusno i tochka"),
        brand("rostics.ru", "Rostic's", "Ростикс"),
        brand("burgerkingrus.ru", "Бургер Кинг", "Burger King"),
        brand("teremok.ru", "Теремок", "Teremok"),
        brand("tanuki.ru", "Тануки", "Tanuki"),
        // Gas stations
        brand("bashneft-azs.ru", "Башнефть", "Bashneft"),
        // Its site has a sharp icon, but not the icon service
        brand("https://teboil.ru/favicon.svg", "Teboil", "Тебойл"),
        // Travel
        brand("aeroflot.ru", "Аэрофлот", "Aeroflot"),
        brand("s7.ru", "S7", "С7"),
        brand("pobeda.aero", "Победа", "Pobeda"),
        brand("www.rzd.ru", "РЖД", "RZD"),
        // Fitness and cinemas
        brand("worldclass.ru", "World Class", "Ворлд Класс"),
        brand("ddxfitness.ru", "DDX Fitness", "DDX"),
        brand("spiritfit.ru", "Spirit Fitness", "Спирит"),
        brand("xfit.ru", "X-Fit", "Икс-Фит"),
        brand("karofilm.ru", "Каро", "KARO"),
        brand("kinomax.ru", "Киномакс", "Kinomax"),
        // Neighbouring countries
        brand("evroopt.by", "Евроопт", "Evroopt"),
        brand("magnum.kz", "Magnum", "Магнум"),
        brand("korzinka.uz", "Korzinka", "Корзинка"),
        // International
        brand("starbucks.com", "Starbucks", "Старбакс"),
        brand("ikea.com", "IKEA", "Икеа"),
        brand("lidl.com", "Lidl", "Лидл"),
        brand("aldi-sued.de", "Aldi", "Алди"),
        brand("tesco.com", "Tesco", "Теско"),
        brand("carrefour.com", "Carrefour", "Карфур"),
        brand("walmart.com", "Walmart"),
        brand("target.com", "Target"),
        brand("decathlon.com", "Decathlon", "Декатлон"),
        brand("sephora.com", "Sephora", "Сефора"),
        brand("hm.com", "H&M"),
        brand("zara.com", "Zara", "Зара"),
        brand("mcdonalds.com", "McDonald's", "Макдоналдс"),
        brand("shell.com", "Shell", "Шелл"),
        brand("boots.com", "Boots"),
        brand("dm.de", "dm"),
        brand("rewe.de", "Rewe"),
        brand("edeka.de", "Edeka"),
        brand("primark.com", "Primark"),
        brand("uniqlo.com", "Uniqlo", "Юникло"),
    )
}
