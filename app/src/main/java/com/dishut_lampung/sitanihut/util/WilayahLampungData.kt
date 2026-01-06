package com.dishut_lampung.sitanihut.util

import com.dishut_lampung.sitanihut.util.WilayahLampungData.data

data class Wilayah(
    val kabupaten: String,
    val kecamatan: List<KecamatanData>
)

data class KecamatanData(
    val nama: String,
    val desa: List<String>
)

object WilayahLampungData {
    fun getKabupatenList(): List<String> {
        return data.map { it.kabupaten }
    }
    fun getKecamatanByKabupaten(kabupaten: String): List<String> {
        return data.find { it.kabupaten == kabupaten }?.kecamatan?.map { it.nama } ?: emptyList()
    }
    fun getDesaByKecamatan(kabupaten: String, kecamatan: String): List<String> {
        return data.find { it.kabupaten == kabupaten }
            ?.kecamatan?.find { it.nama == kecamatan }
            ?.desa ?: emptyList()
    }

    val data = listOf(
        Wilayah(
            kabupaten = "Lampung Selatan",
            kecamatan = listOf(
                KecamatanData("Kalianda", listOf("Way Urang", "Maja", "Kalianda", "Bumi Agung", "Agom", "Pematang", "Suka Mulya", "Palas Jaya", "Marga Sari", "Gunung Terang")),
                KecamatanData("Natar", listOf("Merak Batin", "Pemanggilan", "Hajimena", "Natar", "Muara Putih", "Suka Damai", "Rulung Sari", "Branti Raya", "Krawang Sari", "Negara Ratu")),
                KecamatanData("Jati Agung", listOf("Way Huwi", "Jati Mulyo", "Karang Anyar", "Marga Agung", "Karang Sari", "Margo Mulyo", "Banjar Agung", "Sumber Jaya", "Gedung Harapan", "Sidoasri")),
                KecamatanData("Sidomulyo", listOf("Sidorejo", "Sidomulyo", "Suka Maju", "Cinta Mulya", "Seloretno", "Suak", "Bandar Dalam", "Kota Dalam", "Talang Baru", "Campang Tiga")),
                KecamatanData("Penengahan", listOf("Pasuruan", "Kelaten", "Taman Baru", "Gedung", "Ruang Tengah", "Suka Baru", "Rawi", "Belambangan", "Padan", "Tanjung Heran")),
                KecamatanData("Palas", listOf("Palas Pasemah", "Suka Mulya", "Bali Agung", "Bumi Daya", "Palas Aji", "Bangunan", "Sukamulya", "Tanjung Jaya", "Mekar Mulya", "Rejomulyo")),
                KecamatanData("Sragi", listOf("Kuala Sekampung", "Bakti Rasa", "Sumber Sari", "Marga Jasa", "Mandala Sari", "Suka Pura", "Bandar Agung", "Sumber Agung", "Margasari", "Sriminosari")),
                KecamatanData("Way Panji", listOf("Sidoharjo", "Sidomakmur", "Sidoreno", "Bumi Daya", "Mekar Jaya", "Indra Loka", "Sido Rahayu", "Margo Lestari", "Pandan Sari", "Karya Makmur")),
                KecamatanData("Ketapang", listOf("Legundi", "Ketapang", "Sidoasih", "Pematang Pasir", "Sri Pendowo", "Karang Sari", "Bangun Rejo", "Lebung Nala", "Sumber Nadi", "Tri Darmayoga")),
                KecamatanData("Rajabasa", listOf("Batu Balak", "Canggu", "Canti", "Hargo Pancuran", "Way Muli", "Kerinjing", "Kunjir", "Rajabasa", "Sukaraja", "Wai Kali"))
            )
        ),
        Wilayah(
            kabupaten = "Lampung Tengah",
            kecamatan = listOf(
                KecamatanData("Gunung Sugih", listOf("Gunung Sugih", "Terbanggi Subing", "Wono Sari", "Komering Putih", "Gunung Sari", "Fajar Bulan", "Putra Rumbia", "Buyut Udik", "Gubernur Intan", "Seputih Jaya")),
                KecamatanData("Seputih Banyak", listOf("Tanjung Harapan", "Sakti Buana", "Sumber Bahagia", "Sri Basuki", "Setia Bakti", "Siswa Bangun", "Sumber Fajar", "Tanjung Kerajan", "Rukti Endah", "Sri Mulyani")),
                KecamatanData("Terbanggi Besar", listOf("Bandar Jaya", "Poncowati", "Terbanggi Besar", "Indra Putra Subing", "Karang Endah", "Adi Jaya", "Nambah Dadi", "Bandar Jaya Barat", "Bandar Jaya Timur", "Terbanggi Mulya")),
                KecamatanData("Seputih Mataram", listOf("Kuripan", "Mataram Udik", "Suban", "Sumber Katon", "Trimulyo", "Banjar Agung", "Dharma Agung", "Fajar Mataram", "Karya Bakti", "Qurnia Mataram")),
                KecamatanData("Punggur", listOf("Mojopahit", "Nunggal Rejo", "Punggur", "Sido Mulyo", "Tanggul Angin", "Astomulyo", "Badransari", "Ngesti Rahayu", "Nimpuna", "Toto Katon")),
                KecamatanData("Trimurjo", listOf("Adipuro", "Depok Rejo", "Purwo Dadi", "Pujo Basuki", "Simbar Waringin", "Tempuran", "Trimurjo", "Untoro", "Pujo Asri", "Pujo Kerto")),
                KecamatanData("Anak Tuha", listOf("Negara Aji Tua", "Bumi Aji", "Haji Pemanggilan", "Jaya Sakti", "Mekar Sari", "Negara Aji Baru", "Sri Katon", "Suka Jaya", "Tanjung Harapan", "Gunung Agung")),
                KecamatanData("Kalirejo", listOf("Kalirejo", "Kaliwungu", "Sinoar", "Sri Basuki", "Watu Agung", "Agung Timur", "Bali Rejo", "Kali Dadi", "Suka Sari", "Sendang Asri")),
                KecamatanData("Bekri", listOf("Bekri", "Binjai Agung", "Kedatuan", "Kesuma Jaya", "Rengas", "Sinar Banten", "Gorosubu", "Kesuma Dadi", "Suka Negeri", "Binjai Wangi")),
                KecamatanData("Way Seputih", listOf("Suko Binangun", "Sri Bija", "Sari Bija", "Sakti Bija", "Dharma Bija", "Sinar Bija", "Mekar Bija", "Jaya Bija", "Suka Bija", "Mulia Bija"))
            )
        ),
        Wilayah(
            kabupaten = "Lampung Utara",
            kecamatan = listOf(
                KecamatanData("Kotabumi", listOf("Sribasuki", "Kotabumi Ilir", "Kotabumi Udik", "Tanjung Aman", "Kotabumi Pasar", "Cempedak", "Talang Bojong", "Sawojajar", "Suka Sari", "Tanjung Harapan")),
                KecamatanData("Abung Selatan", listOf("Kalibalangan", "Trimodadi", "Gilih Suka Negeri", "Kemalo Abung", "Abung Jayo", "Bandar Kagungan Raya", "Cabang Empat", "Kembang Tanjung", "Sinar Ogan", "Way Lunik")),
                KecamatanData("Bukit Kemuning", listOf("Bukit Kemuning", "Dwikora", "Suka Menanti", "Muara Dua", "Tanjung Baru", "Gunung Sari", "Sido Mulyo", "Cahaya Negeri", "Harapan Jaya", "Bukit Sari")),
                KecamatanData("Sungkai Selatan", listOf("Banjar Ketapang", "Bumi Ratu", "Gedung Ketapang", "Karang Rejo", "Ketapang", "Kota Agung", "Labuhanratu", "Negara Agung", "Sidodadi", "Sinar Galih")),
                KecamatanData("Abung Timur", listOf("Banjar Agung", "Bumi Agung Marga", "Bumi Jaya", "Pungguk Lama", "Rejo Mulyo", "Sumber Agung", "Surakarta", "Peraduan Waras", "Sidomukti", "Sidomulyo")),
                KecamatanData("Sungkai Utara", listOf("Negara Ratu", "Batu Raja", "Ciamis", "Gedung Batin", "Hanum", "Kota Negara", "Negeri Sakti", "Ogan Jaya", "Padang Ratu", "Bangun Jaya")),
                KecamatanData("Kotabumi Utara", listOf("Madukoro", "Margorejo", "Sawojajar", "Talangjali", "Wonomarto", "Banjarwangi", "Kalicinta", "Sidorahayu", "Sumber Arum", "Tanjung Pura")),
                KecamatanData("Abung Barat", listOf("Gunung Betuah", "Kistang", "Lepang Besar", "Ogan Lima", "Pematang Kasih", "Pengaringan", "Simpang Agung", "Tanjung Harta", "Way Wakak", "Sumber Tani")),
                KecamatanData("Sungkai Jaya", listOf("Cahaya Makmur", "Sinar Harapan", "Sri Agung", "Sukai Menanti", "Mekar Sari", "Mulyo Rejo", "Karya Jaya", "Sinar Baru", "Cempaka", "Sido Harjo")),
                KecamatanData("Blambangan Pagar", listOf("Blambangan", "Bojong", "Jagang", "Kalibalangan", "Pagar", "Pagar Gading", "Tanjung Iman", "Suka Jadi", "Suka Mulya", "Suka Bakti"))
            )
        ),
        Wilayah(
            kabupaten = "Lampung Barat",
            kecamatan = listOf(
                KecamatanData("Balik Bukit", listOf("Pasar Liwa", "Wates", "Kubu Perahu", "Padang Cahya", "Way Empulau Ulu", "Sebarus", "Gunung Sugih", "Padang Dalom", "Bahway", "Sukarame")),
                KecamatanData("Sekincau", listOf("Pampangan", "Giham Sukamaju", "Tiga Jaya", "Waspada", "Pagar Dewa", "Suka Banjar", "Tri Mulyo", "Pancur Mas", "Batu Api", "Sinar Jaya")),
                KecamatanData("Batu Brak", listOf("Pekon Balak", "Kembahang", "Gunung Sugih", "Kota Besi", "Teba Liyokh", "Canggu", "Negeri Ratu", "Gunung Terang", "Tanjung Jati", "Suka Marga")),
                KecamatanData("Suoh", listOf("Bandar Agung", "Suka Marga", "Tuguratu", "Ringin Sari", "Sumber Agung", "Bumi Hantatai", "Sido Rejo", "Karya Maju", "Sari Makmur", "Mekar Sari")),
                KecamatanData("Belalau", listOf("Kenali", "Hujung", "Serungkuk", "Bedudu", "Bumi Agung", "Turgak", "Suka Makmur", "Tanjung Raya", "Suka Damai", "Pagar Agung")),
                KecamatanData("Way Tenong", listOf("Mutar Alam", "Pajar Bulan", "Sukananti", "Tambak Jaya", "Karang Agung", "Suka Jadi", "Tanjung Sari", "Suka Marga", "Padang Tambak", "Mutar Jaya")),
                KecamatanData("Sumber Jaya", listOf("Tugu Sari", "Sindang Pagar", "Way Petai", "Simpang Sari", "Karya Sari", "Mekar Jaya", "Suka Pura", "Pura Jaya", "Pura Wiwitan", "Sumber Sari")),
                KecamatanData("Pagar Dewa", listOf("Pagar Dewa", "Basungan", "Suka Jaya", "Marga Jaya", "Pahayu Jaya", "Suka Mulya", "Suka Ramai", "Suka Agung", "Suka Makmur", "Suka Damai")),
                KecamatanData("Lumbok Seminung", listOf("Lumbok", "Suka Banjar", "Suka Maju", "Heni Arong", "Tawan Suka Mulya", "Pancur Mas", "Suka Marga", "Ujung Rembun", "Tanjung Jati", "Sukarame")),
                KecamatanData("Gedung Surian", listOf("Gedung Surian", "Mekar Jaya", "Pura Mekar", "Cipta Waras", "Trimulyo", "Suka Damai", "Gedung Sari", "Sari Mulyo", "Karya Bakti", "Suka Jadi"))
            )
        ),
        Wilayah(
            kabupaten = "Tulang Bawang",
            kecamatan = listOf(
                KecamatanData("Menggala", listOf("Menggala Kota", "Ujung Gunung Ilir", "Menggala Selatan", "Tiuh Tohou", "Bujung Tenuk", "Kagungan Rahayu", "Astra Ksetra", "Cempaka Dalam", "Bujung Burung", "Pasar Baru")),
                KecamatanData("Gedung Aji", listOf("Aji Jaya", "Gedung Aji", "Aji Mesir", "Penawar", "Aji Murni", "Aji Permai", "Suka Bakti", "Karya Makmur", "Budi Sari", "Harapan Mulya")),
                KecamatanData("Banjar Agung", listOf("Banjar Agung", "Dwi Warga Tunggal Jaya", "Tri Dharma Wirajaya", "Warga Makmur Jaya", "Tunggal Warga", "Banjar Dewa", "Tri Mukti Jaya", "Tri Mulya Jaya", "Tri Tunggal Jaya", "Warga Indah Jaya")),
                KecamatanData("Gedung Meneng", listOf("Gedung Meneng", "Bakung Ilir", "Bakung Udik", "Gedung Bandar", "Kuala", "Pasiran Jaya", "Sido Mulyo", "Sumber Makmur", "Gunung Tapa", "Wono Rejo")),
                KecamatanData("Dente Teladas", listOf("Way Dente", "Pendowo Asri", "Teladas", "Sungai Nibung", "Kuala Teladas", "Mahabang", "Kekatung", "Pasir Jaya", "Brata Sena", "Suka Jaya")),
                KecamatanData("Penawar Tama", listOf("Bogatama", "Wira Agung Sari", "Tri Jaya", "Pulo Gadung", "Sidomakmur", "Sidomulyo", "Sidoharjo", "Trikarya", "Wiratama", "Wira Jaya")),
                KecamatanData("Rawajitu Selatan", listOf("Medasari", "Gedung Karya Jitu", "Bumi Dipasena", "Yudha Karya Jitu", "Wono Agung", "Karya Jitu", "Hargo Rejo", "Suka Bhakti", "Mulyo Asri", "Karya Makmur")),
                KecamatanData("Rawajitu Timur", listOf("Bumi Sari", "Bumi Sentosa", "Rajawali", "Sumber Sari", "Suka Bakti", "Gajah Mati", "Panggung Rejo", "Panggung Jaya", "Sido Mukti", "Mekar Sari")),
                KecamatanData("Rawa Pitu", listOf("Andalas Cermin", "Batang Hari", "Bumi Sari", "Gedung Jaya", "Mulyo Dadi", "Panggung Mulya", "Rawa Ragil", "Sumber Agung", "Yoso Mulyo", "Karya Agung")),
                KecamatanData("Penawar Aji", listOf("Gedung Harapan", "Gedung Rejo Sakti", "Karya Makmur", "Panca Tunggal Jaya", "Suka Makmur", "Sumber Sari", "Wono Rejo", "Sari Agung", "Mekar Jaya", "Budi Daya"))
            )
        ),
        Wilayah(
            kabupaten = "Tanggamus",
            kecamatan = listOf(
                KecamatanData("Kota Agung", listOf("Kelungu", "Kusa", "Pasar Madang", "Teratas", "Negeri Ratu", "Kota Agung", "Pardasuka", "Terdana", "Kota Batu", "Penanggungan")),
                KecamatanData("Wonosobo", listOf("Pekon Wonosobo", "Sridadi", "Kalirejo", "Tanjung Ratu", "Bandar Sukabumi", "Dadapan", "Kunyayan", "Soponyono", "Padang Manis", "Gunung Sari")),
                KecamatanData("Semaka", listOf("Sukaraja", "Kanoman", "Tugurejo", "Sedayu", "Way Kerap", "Pardawaras", "Sri Katon", "Garut", "Suka Banjar", "Kacapura")),
                KecamatanData("Talang Padang", listOf("Talang Padang", "Negeri Agung", "Bandar Negeri Semuong", "Sinar Semendo", "Suka Bandung", "Kalibening", "Sinar Banten", "Suka Merindah", "Talang Sewu", "Way Halom")),
                KecamatanData("Pulau Panggung", listOf("Tekad", "Tanjung Begelung", "Gunung Meraksa", "Air Naningan", "Sinar Marga", "Batu Bedil", "Sinar Agung", "Talang Jawa", "Suka Damai", "Gunung Tiga")),
                KecamatanData("Cukuh Balak", listOf("Putih Doh", "Tanjung Jati", "Tanjung Raja", "Pekon Ampai", "Banjar Manis", "Suka Padang", "Kacamarga", "Sukarame", "Suka Banjar", "Way Tuba")),
                KecamatanData("Pugung", listOf("Pugung", "Rantau Tijang", "Tiuh Memon", "Gunung Kasih", "Suka Jadi", "Talang Lebar", "Way Jaha", "Pagar Alam", "Suka Mulya", "Tanjung Agung")),
                KecamatanData("Gisting", listOf("Gisting Atas", "Gisting Bawah", "Kuta Dalom", "Gisting Permai", "Campang", "Landbaw", "Sido Rahayu", "Suka Maju", "Purwodadi", "Banjar Sari")),
                KecamatanData("Air Naningan", listOf("Air Naningan", "Datar Lebuay", "Sinar Jaya", "Sinar Sekampung", "Way Harong", "Suka Negeri", "Pekon Ampai", "Margomulyo", "Karya Makmur", "Bumi Agung")),
                KecamatanData("Ulu Belu", listOf("Ngarip", "Gunung Tiga", "Sinar Banten", "Tanjung Baru", "Ulu Semong", "Datarajan", "Pagar Alam", "Sirna Galih", "Suka Maju", "Gunung Sari"))
            )
        ),
        Wilayah(
            kabupaten = "Lampung Timur",
            kecamatan = listOf(
                KecamatanData("Sukadana", listOf("Sukadana", "Pasar Sukadana", "Negara Nabung", "Muara Jaya", "Pakuan Aji", "Mataram Marga", "Bumi Nabung", "Rantau Jaya", "Surabaya Udik", "Sukadana Ilir")),
                KecamatanData("Way Jepara", listOf("Labuhan Ratu", "Braja Sakti", "Sumber Rejo", "Sri Gading", "Jepara", "Labuhan Ratu Baru", "Sumber Marga", "Tanjung Wangi", "Suka Marga", "Mekar Jaya")),
                KecamatanData("Pekalongan", listOf("Pekalongan", "Siraman", "Adirejo", "Gantiwarno", "Tulusrejo", "Gondangrejo", "Kalibening", "Wonokarto", "Sidodadi", "Jojog")),
                KecamatanData("Labuhan Maringgai", listOf("Labuhan Maringgai", "Maringgai", "Karya Makmur", "Karya Tani", "Suka Damai", "Bandar Negeri", "Muara Gading Mas", "Sri Gading", "Tanjung Aji", "Pantai Timur")),
                KecamatanData("Jabung", listOf("Negara Batin", "Jabung", "Gunung Mekar", "Tanjung Sari", "Mekar Jaya", "Pematang Tahalo", "Adi Luhur", "Sukaraja", "Gunung Sugih", "Bumi Harapan")),
                KecamatanData("Batanghari", listOf("Banarjoyo", "Batangharjo", "Bumi Harjo", "Selorejo", "Sumberrejo", "Rejoagung", "Telogorejo", "Bhuwana Bakti", "Nampirejo", "Sari Mulyo")),
                KecamatanData("Sekampung", listOf("Sekampung", "Sumber Gede", "Giri Klopo Mulyo", "Tanjung Harapan", "Sido Mulyo", "Sambikarto", "Tritunggal", "Waringin Sari", "Mekar Sari", "Karya Basuki")),
                KecamatanData("Metro Kibang", listOf("Kibang", "Purbosembodo", "Sumber Agung", "Margasari", "Marga Jaya", "Gantimulyo", "Margo Toto", "Mekar Sari", "Mulyo Asri", "Jaya Asri")),
                KecamatanData("Waway Karya", listOf("Jembrana", "Karya Basuki", "Marga Batin", "Sumber Jaya", "Tanjung Wangi", "Karang Anom", "Ngesti Karya", "Sido Makmur", "Suka Bakti", "Tri Tunggal")),
                KecamatanData("Gunung Pelindung", listOf("Negeri Agung", "Pelindung Jaya", "Way Mili", "Nibung", "Pempen", "Suka Damai", "Tanjung Tirto", "Mekar Sari", "Sinar Indah", "Karya Mulyo"))
            )
        ),
        Wilayah(
            kabupaten = "Way Kanan",
            kecamatan = listOf(
                KecamatanData("Blambangan Umpu", listOf("Umpu Kencana", "Sri Rejeki", "Lembasung", "Blambangan Umpu", "Tanjung Raja", "Giham", "Umpu Bhakti", "Karang Umpu", "Bumi Ratu", "Segara Mider")),
                KecamatanData("Kasui", listOf("Jaya Tinggi", "Kasui Lama", "Sukamaju", "Talang Mangga", "Datar Bancong", "Bukit Batu", "Sinar Gading", "Tanjung Kurung", "Gunung Sari", "Mekar Asri")),
                KecamatanData("Baradatu", listOf("Bumi Merapi", "Gunung Katun", "Tiuh Balak", "Banjarmasin", "Bumi Rejo", "Cugah", "Gedung Rejo", "Setia Negara", "Suka Negeri", "Tiuh Balak Pasar")),
                KecamatanData("Pakuan Ratu", listOf("Pakuan Ratu", "Negara Tama", "Bumi Mulya", "Gunung Cahya", "Rumbih", "Serupa Indah", "Suka Agung", "Gunung Putri", "Tanjung Agung", "Way Tawar")),
                KecamatanData("Negeri Agung", listOf("Negeri Agung", "Bandar Dalam", "Bumi Say Agung", "Kali Papan", "Kota Jawa", "Mulya Sari", "Rejo Sari", "Sungsang", "Way Limau", "Pulau Batu")),
                KecamatanData("Way Tuba", listOf("Way Tuba", "Bumi Dana", "Bukit Gemuruh", "Karta Jaya", "Ramsai", "Say Umpu", "Suma Mukti", "Tanjung Raja", "Way Pisang", "Tanjung Sari")),
                KecamatanData("Banjit", listOf("Pasar Banjit", "Baliangan", "Bonglai", "Campur Asri", "Donomulyo", "Juku Kemuning", "Kemu", "Menanga Siamang", "Rantau Temiang", "Sumber Sari")),
                KecamatanData("Gunung Labuhan", listOf("Gunung Labuhan", "Gunung Sari", "Kayu Batu", "Labuhan Jaya", "Negeri Mulya", "Suka Rame", "Tiuh Balak II", "Way Tuba", "Suka Negeri", "Mekar Jaya")),
                KecamatanData("Negara Batin", listOf("Negara Batin", "Adi Jaya", "Bumi Jaya", "Gisting Jaya", "Karta Jaya", "Marga Jaya", "Negara Mulya", "Sari Jaya", "Setia Marga", "Suka Maju")),
                KecamatanData("Rebang Tangkas", listOf("Tanjung Tiga", "Air Ringkih", "Beringin Jaya", "Gunung Sari", "Karya Maju", "Lebak Peniangan", "Madang", "Simpang Tiga", "Tanjung Sari", "Suka Jadi"))
            )
        ),
        Wilayah(
            kabupaten = "Pesawaran",
            kecamatan = listOf(
                KecamatanData("Gedong Tataan", listOf("Bagelen", "Sukaraja", "Wiyono", "Gedong Tataan", "Bernung", "Cipadang", "Karang Anyar", "Kebayoran", "Padang Ratu", "Suka Banjar")),
                KecamatanData("Way Lima", listOf("Batu Raja", "Cimanuk", "Padang Cermin", "Gunung Rejo", "Banjar Negeri", "Gedung Dalom", "Kuta Dalom", "Sindang Garut", "Suka Mandi", "Tanjung Rejo")),
                KecamatanData("Punduh Pidada", listOf("Bawang", "Bandar Dalam", "Kota Jawa", "Pagar Jaya", "Pulau Legundi", "Suka Maju", "Tanjung Agung", "Tanjung Kerta", "Way Asahan", "Tanjung Sari")),
                KecamatanData("Marga Punduh", listOf("Kunyayan", "Maja", "Pekon Ampai", "Pekon Unggak", "Sukajaya Punduh", "Tanjung Kerta", "Umbul Limus", "Gunung Rejo", "Suka Damai", "Suka Bakti")),
                KecamatanData("Padang Cermin", listOf("Padang Cermin", "Durian", "Gayau", "Hurun", "Khepong Jaya", "Paya", "Sanggi", "Tambangan", "Way Urang", "Trimulyo")),
                KecamatanData("Teluk Pandan", listOf("Cilimus", "Gebang", "Hurun", "Munca", "Sidodadi", "Suka Jaya Lempasing", "Talang Mulya", "Tanjung Agung", "Batu Menyan", "Suka Jadi")),
                KecamatanData("Way Khilau", listOf("Bayas", "Gunung Sari", "Kota Dalom", "Padang Cermin", "Penengahan", "Tanjung Kerta", "Way Khilau", "Suka Jaya", "Mada Jaya", "Tanjung Rejo")),
                KecamatanData("Tegineneng", listOf("Tegineneng", "Batang Hari Ogan", "Bumi Agung", "Gedung Gumanti", "Gerning", "Gunung Sugih Baru", "Kota Agung", "Margomulyo", "Negeri Katon", "Sinar Jati")),
                KecamatanData("Negeri Katon", listOf("Negeri Katon", "Bangun Sari", "Halangan Ratu", "Kalirejo", "Lumbirejo", "Negara Saka", "Pejambon", "Pujorahayu", "Roworejo", "Sidomulyo")),
                KecamatanData("Way Ratai", listOf("Way Ratai", "Bunut", "Ceringin Asri", "Gunung Rejo", "Hurun", "Mulyosari", "Pesawaran Indah", "Poncorejo", "Suka Damai", "Sumber Jaya"))
            )
        ),
        Wilayah(
            kabupaten = "Pringsewu",
            kecamatan = listOf(
                KecamatanData("Pringsewu", listOf("Pringsewu Selatan", "Pringsewu Barat", "Waluyojati", "Fajar Agung", "Pringsewu Timur", "Rejosari", "Sidoharjo", "Podomoro", "Bumi Arum", "Margakaya")),
                KecamatanData("Gading Rejo", listOf("Gading Rejo", "Wonosari", "Blitarejo", "Tambahrejo", "Panjerejo", "Tulung Agung", "Wates", "Kediri", "Yogyakarta", "Suka Damai")),
                KecamatanData("Ambarawa", listOf("Ambarawa", "Kresnomulyo", "Sumber Agung", "Tanjung Anom", "Margodadi", "Ambarawa Barat", "Ambarawa Timur", "Jati Agung", "Sari Rejo", "Sinar Waya")),
                KecamatanData("Pardasuka", listOf("Pardasuka", "Suka Negeri", "Waringinsari", "Pujodadi", "Rantau Tijang", "Selapan", "Sidodadi", "Suka Marga", "Tanjung Rusia", "Kedaung")),
                KecamatanData("Pagelaran", listOf("Pagelaran", "Bumi Ratu", "Pamenang", "Panutan", "Pasir Ukir", "Suka Ratu", "Sumber Rejo", "Way Ngison", "Gumukmas", "Candiretno")),
                KecamatanData("Banyumas", listOf("Banyumas", "Banjarejo", "Nusa Wungu", "Sinar Mulya", "Sri Rahayu", "Sukomulyo", "Tri Bakti", "Waya Krui", "Mulyo Asri", "Mekar Sari")),
                KecamatanData("Adiluwih", listOf("Adiluwih", "Bandung Baru", "Enggal", "Purwodadi", "Sinar Waya", "Srikaton", "Sukoharum", "Tegalsari", "Tritunggal", "Wonosari")),
                KecamatanData("Sukoharjo", listOf("Sukoharjo I", "Sukoharjo II", "Sukoharjo III", "Panggungrejo", "Siliwangi", "Pandansari", "Sinar Baru", "Waringinsari Barat", "Keputran", "Pandan Surat")),
                KecamatanData("Pagelaran Utara", listOf("Fajar Mulia", "Giri Tunggal", "Gunung Raya", "Kamilin", "Mada Jaya", "Margo Mulyo", "Neglasari", "Sinar Jaya", "Sumber Bandung", "Wai Kunang")),
                KecamatanData("Bantul", listOf("Bantul", "Sinar Seputih", "Rejosari", "Sriwedari", "Pandan Sari", "Margo Rejo", "Sido Mulyo", "Karya Agung", "Bakti Sosial", "Tirto Kencono"))
            )
        ),
        Wilayah(
            kabupaten = "Mesuji",
            kecamatan = listOf(
                KecamatanData("Simpang Pematang", listOf("Simpang Pematang", "Budi Aji", "Harapan Jaya", "Simpang Mesuji", "Margo Makmur", "Margo Rahayu", "Wirabangun", "Adi Luhur", "Jaya Sakti", "Wira Bangun")),
                KecamatanData("Way Serdang", listOf("Labuhan Mulya", "Kejadian", "Buko Poso", "Sumber Rejo", "Suka Agung", "Gedung Boga", "Panca Warna", "Labuhan Baru", "Suka Mandiri", "Tanjung Mas")),
                KecamatanData("Tanjung Raya", listOf("Brabasan", "Mekar Sari", "Muara Tenang", "Tanjung Harapan", "Tri Karya Mulya", "Wira Jaya", "Adi Karya Mulya", "Bujung Buring", "Gedung Ram", "Harapan Mukti")),
                KecamatanData("Mesuji Timur", listOf("Tanjung Mas Makmur", "Eka Mulya", "Margo Jadi", "Panggung Jaya", "Tanjung Mas Jaya", "Sumber Makmur", "Wonorejo", "Tanjung Mas Mulya", "Muara Mas", "Sungai Cambai")),
                KecamatanData("Panca Jaya", listOf("Adi Luhur", "Fajar Asri", "Fajar Baru", "Fajar Indah", "Jaya Sakti", "Mukti Karya", "Adi Mulyo", "Budi Makmur", "Harapan Mukti", "Margo Bhakti")),
                KecamatanData("Mesuji", listOf("Wiralaga Mulya", "Sungai Badak", "Sido Mulyo", "Tirtalaga", "Wiralaga I", "Wiralaga II", "Tanjung Serayan", "Mulia Sari", "Nibung", "Gedung Mulya")),
                KecamatanData("Rawa Jitu Utara", listOf("Panggung Rejo", "Sidang Sido Rahayu", "Sidang Iso Mukti", "Sidang Kurnia Agung", "Sidang Muara Jaya", "Sidang Sidorahayu", "Telogo Rejo", "Panggung Jaya", "Sido Agung", "Karya Jitu")),
                KecamatanData("Tanjung Sari", listOf("Tanjung Sari", "Suka Damai", "Mekar Jaya", "Karya Makmur", "Sinar Harapan", "Mulyo Rejo", "Bakti Sosial", "Cahaya Mas", "Tirta Kencana", "Sumber Rejeki")),
                KecamatanData("Pematang Panggang", listOf("Pematang Panggang", "Sumber Agung", "Margo Mulyo", "Jaya Bakti", "Suka Negeri", "Harapan Makmur", "Cipta Daya", "Karya Indah", "Sinar Mulya", "Budi Luhur")),
                KecamatanData("Adi Karya", listOf("Adi Karya", "Mekar Sari", "Sido Rahayu", "Margo Utomo", "Tanjung Rejo", "Suka Jadi", "Bumi Aji", "Panca Marga", "Sumber Sari", "Giri Mulyo"))
            )
        ),
        Wilayah(
            kabupaten = "Tulang Bawang Barat",
            kecamatan = listOf(
                KecamatanData("Tulang Bawang Tengah", listOf("Panaragan Jaya", "Tirta Makmur", "Candra Kencana", "Panaragan", "Pulung Kencana", "Tirta Kencana", "Bandar Dewa", "Menggala Mas", "Murni Jaya", "Wono Kerto")),
                KecamatanData("Tulang Bawang Udik", listOf("Tunas Asri", "Karta", "Marga Kencana", "Daya Murni", "Gedung Ratu", "Kartaraharja", "Marga Asri", "Penumangan", "Suka Jaya", "Way Sido")),
                KecamatanData("Lambu Kibang", listOf("Lambu Kibang", "Gilang Tunggal Makarta", "Gunung Sari", "Kibang Budi Jaya", "Kibang Mulya Jaya", "Lesung Bhakti Jaya", "Mekar Sari Jaya", "Pagar Buana", "Sumber Rejo", "Tirta Mulya")),
                KecamatanData("Gunung Terang", listOf("Gunung Terang", "Gunung Agung", "Kagungan Jaya", "Margo Dadi", "Mulyo Jadi", "Setia Agung", "Setia Bumi", "Terang Bumi Agung", "Terang Makmur", "Terang Mulya")),
                KecamatanData("Pagar Dewa", listOf("Pagar Dewa", "Bujung Sari", "Cahyou Randu", "Marga Jaya", "Pagar Dewa Suka Mulya", "Suka Jaya", "Suka Mulya", "Sumber Rejo", "Tunas Jaya", "Mekar Sari")),
                KecamatanData("Gunung Agung", listOf("Tunas Jaya", "Dwi Mulyo", "Marga Mulya", "Mulyo Dadi", "Suka Jaya", "Sumber Rejeki", "Tri Tunggal Jaya", "Wono Rejo", "Karya Bhakti", "Suka Makmur")),
                KecamatanData("Way Kenanga", listOf("Balam Jaya", "Balam Asri", "Gading Sari", "Indraloka I", "Indraloka II", "Mercu Buana", "Pagarbuana", "Sido Mulyo", "Suka jadi", "Tanjung Sari")),
                KecamatanData("Batu Putih", listOf("Batu Putih", "Margo Dadi", "Margo Mulyo", "Mulyo Sari", "Panca Marga", "Sakti Jaya", "Sido Makmur", "Suka Jadi", "Sumber Rejeki", "Tirta Makmur")),
                KecamatanData("Tumijajar", listOf("Dayasakti", "Gunungsari", "Makarti", "Margodadi", "Margomulyo", "Margosari", "Mulyoasri", "Sumbersari", "Tunas Asri", "Dayamurni")),
                KecamatanData("Sendang Agung", listOf("Sendang Agung", "Sendang Asri", "Sendang Mulyo", "Sendang Rejo", "Suka Bhakti", "Karya Jaya", "Mekar Sari", "Sido Rahayu", "Sumber Makmur", "Tirta Agung"))
            )
        ),
        Wilayah(
            kabupaten = "Pesisir Barat",
            kecamatan = listOf(
                KecamatanData("Pesisir Tengah", listOf("Pasar Krui", "Rawas", "Seray", "Way Redak", "Pahmungan", "Suka Negara", "Kampung Jawa", "Gunung Kemala", "Suka Banjar", "Padang Haluan")),
                KecamatanData("Pesisir Selatan", listOf("Biha", "Marang", "Pagar Dalam", "Sukarame", "Tanjung Jati", "Way Jambu", "Batu Raja", "Bangun Negara", "Negeri Ratu", "Ulu Krui")),
                KecamatanData("Karya Penggawa", listOf("Way Sindi", "Penggawa V", "Penggawa IV", "Kebuaian", "Laay", "Menyancang", "Penengahan", "Suka Maju", "Tanjung Raya", "Suka Damai")),
                KecamatanData("Lemong", listOf("Lemong", "Bambang", "Cahaya Negeri", "Malaya", "Pagar Dalam", "Parda Haga", "Rata Agung", "Suka Mulya", "Tanjung Jati", "Way Batang")),
                KecamatanData("Pesisir Utara", listOf("Kota Karang", "Walur", "Batu Raja", "Kerbang Dalam", "Kerbang Langgar", "Kuripan", "Padang Rindu", "Pahmungan", "Suka Jadi", "Tanjung Kemala")),
                KecamatanData("Pulau Pisang", listOf("Pekon Pisang", "Labuhan", "Sukarame", "Bandar Dalam", "Suka Marga", "Pasar Pulau", "Suka Damai", "Maju Jaya", "Bakti Sosial", "Tirta Kencana")),
                KecamatanData("Ngambur", listOf("Ngambur", "Sukarame", "Gedau", "Sumberejo", "Neglasari", "Ulu Balam", "Way Sindi Hanuan", "Suka Banjar", "Parda Suka", "Tanjung Setia")),
                KecamatanData("Bangkunat", listOf("Pemerihan", "Way Haru", "Kota Jawa", "Pagar Bukit", "Suka Negeri", "Sumberejo", "Tanjung Kemala", "Way Tiyas", "Suka Marga", "Mekar Sari")),
                KecamatanData("Ngaras", listOf("Ngaras", "Bandar Jaya", "Kota Batu", "Mulang Maya", "Negeri Ratu Ngaras", "Padang Alam", "Parda Suka", "Raja Basa", "Suka Maju", "Tanjung Pura")),
                KecamatanData("Way Krui", listOf("Gunung Kemala Timur", "Pajar Bulan", "Labuhan Mandi", "Suka Jadi", "Way Krui", "Banjar Agung", "Bumi Waras", "Suka Sari", "Mekar Jaya", "Tanjung Rejo"))
            )
        ),
        Wilayah(
            kabupaten = "Kota Bandar Lampung",
            kecamatan = listOf(
                KecamatanData("Teluk Betung Barat", listOf("Sukarame", "Bakung", "Kuripan", "Pesawahan", "Teluk Betung", "Bumi Waras", "Garuntang", "Kangkung", "Perwata", "Suka Jaya")),
                KecamatanData("Kemiling", listOf("Kemiling Permai", "Beringin Raya", "Pinang Jaya", "Sumber Rejo", "Kedaung", "Beringin Jaya", "Sumberejo Sejahtera", "Raja Basa", "Tanjung Gading", "Sukarame Baru")),
                KecamatanData("Tanjung Karang Pusat", listOf("Durian Payung", "Gotong Royong", "Palapa", "Kelapa Tiga", "Pasir Gintung", "Kaliawi", "Sawah Brebes", "Tanjung Karang", "Enggal", "Gunung Sari")),
                KecamatanData("Panjang", listOf("Panjang Utara", "Panjang Selatan", "Karang Maritim", "Srengsem", "Pidada", "Way Lunik", "Keteguhan", "Suka Indah", "Sumber Agung", "Bumi Kedamaian")),
                KecamatanData("Rajabasa", listOf("Rajabasa", "Rajabasa Jaya", "Rajabasa Nunyai", "Rajabasa Pemuka", "Gedong Meneng", "Kampung Baru", "Labuhan Ratu", "Sepang Jaya", "Tanjung Senang", "Way Dadi")),
                KecamatanData("Sukarame", listOf("Sukarame", "Way Dadi", "Korpri Jaya", "Harapan Jaya", "Nusantara Permai", "Perumnas Way Kandis", "Sukarame Baru", "Tanjung Baru", "Mekar Jaya", "Suka Jadi")),
                KecamatanData("Kedaton", listOf("Kedaton", "Penengahan", "Sidodadi", "Surabaya", "Kampung Baru", "Labuhan Ratu Raya", "Penengahan Raya", "Suka Menanti", "Tanjung Raya", "Suka Indah")),
                KecamatanData("Sukaraja", listOf("Sukaraja", "Bumi Kedaton", "Jagabaya I", "Jagabaya II", "Jagabaya III", "Way Halim Permai", "Gunung Sulah", "Kedamaian", "Suka Jawa", "Suka Danaham")),
                KecamatanData("Bumi Waras", listOf("Bumi Waras", "Sukaraja", "Kangkung", "Garuntang", "Pesawahan", "Bumi Raya", "Suka Damai", "Pahoman", "Way Tataan", "Tanjung Harapan")),
                KecamatanData("Langkapura", listOf("Langkapura", "Gunung Terang", "Langkapura Baru", "Sukarame II", "Bilabong Jaya", "Gunung Agung", "Sinar Mulya", "Karya Bakti", "Cipta Sari", "Maju Jaya"))
            )
        ),
        Wilayah(
            kabupaten = "Kota Metro",
            kecamatan = listOf(
                KecamatanData("Metro Timur", listOf("Yosodadi", "Iringmulyo", "Yosomulyo", "Iringmulyo Timur", "Tejoagung", "Tejosari", "Banjar Sari", "Karya Makmur", "Mulia Asri", "Yosorejo")),
                KecamatanData("Metro Barat", listOf("Ganjar Asri", "Mulyojati", "Ganjar Agung", "Mulyosari", "Ganjar Asri Barat", "Sumbersari", "Karangrejo", "Metro", "Suka Damai", "Sari Mulyo")),
                KecamatanData("Metro Pusat", listOf("Metro", "Imopuro", "Hadimulyo Barat", "Hadimulyo Timur", "Yosodadi", "Kauman", "Banjar Sari", "Margorejo", "Rejomulyo", "Suka Maju")),
                KecamatanData("Metro Selatan", listOf("Margodadi", "Rejomulyo", "Sumbersari", "Margorejo", "Banjarsari", "Ganjaragung", "Karangrejo", "Mulyojati", "Mulyosari", "Sumbersari Bantul")),
                KecamatanData("Metro Utara", listOf("Banjarsari", "Karangrejo", "Purwosari", "Purwoasri", "Karangrejo Utara", "Sumber Agung", "Bantul", "Sinar Mulyo", "Karya Murni", "Tirta Kencana")),
                KecamatanData("Hadimulyo", listOf("Hadimulyo Barat", "Hadimulyo Timur", "Karang Sari", "Pandanwangi", "Tegalrejo", "Sumber Sari", "Sinar Banten", "Jaya Bakti", "Suka Jadi", "Mekar Sari")),
                KecamatanData("Ganjar Agung", listOf("Ganjar Agung", "Ganjar Asri", "Sido Mulyo", "Karya Indah", "Maju Mapan", "Bumi Asih", "Tirto Mulyo", "Suka Rahayu", "Marga Jaya", "Tanjung Sari")),
                KecamatanData("Yosodadi", listOf("Yosodadi", "Yosomulyo", "Yosorejo", "Sido Rahayu", "Mekar Asri", "Pandan Asri", "Sari Mukti", "Margo Rahayu", "Bumi Daya", "Karya Agung")),
                KecamatanData("Iringmulyo", listOf("Iringmulyo", "Iringmulyo Timur", "Pulo Sari", "Cipta Marga", "Budi Luhur", "Sinar Harapan", "Harapan Jaya", "Suka Mulya", "Margo Dadi", "Sumber Jaya")),
                KecamatanData("Mulyojati", listOf("Mulyojati", "Mulyosari", "Karangrejo", "Sumbersari", "Sari Rejo", "Sinar Rejo", "Rejo Asri", "Pandan Rejo", "Bumi Rejo", "Karya Rejo"))
            )
        )
    )
}