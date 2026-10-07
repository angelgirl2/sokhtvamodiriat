package com.angelgirlbrand.modiratsokhtandestelam

import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.util.VehicleQrParser
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VehicleQrParserTest {

    @Test
    fun testParseJsonVehicleQr() {
        val jsonPayload = """{"title":"پژو ۲۰۶ تیپ ۲","plate":"۲۴ب۸۵۲-۶۸","vin":"IRPEU206140309871","fuelType":"بنزین معمولی","capacity":50,"type":"خودرو"}"""
        val result = VehicleQrParser.parse(jsonPayload)

        assertEquals("پژو ۲۰۶ تیپ ۲", result.title)
        assertEquals("24", result.plateFirst2)
        assertEquals("ب", result.plateLetter)
        assertEquals("852", result.plateLast3)
        assertEquals("68", result.plateCityCode)
        assertEquals(50.0, result.tankCapacity, 0.1)
        assertEquals(VehicleEntity.TYPE_CAR, result.vehicleType)
    }

    @Test
    fun testParseKeyValueVehicleQr() {
        val kvPayload = "MODEL:سمند LX;PLATE:45د763-21;VIN:IRSAM14028761234;CAP:60;FUEL:بنزین سوپر"
        val result = VehicleQrParser.parse(kvPayload)

        assertEquals("سمند LX", result.title)
        assertEquals("45", result.plateFirst2)
        assertEquals("د", result.plateLetter)
        assertEquals("763", result.plateLast3)
        assertEquals("21", result.plateCityCode)
        assertEquals(60.0, result.tankCapacity, 0.1)
    }

    @Test
    fun testParseMotorcycleQr() {
        val motoPayload = """{"title":"موتورسیکلت کلیک","plate":"۲۱۵-۴۸۹۶۳","type":"موتورسیکلت","capacity":6}"""
        val result = VehicleQrParser.parse(motoPayload)

        assertEquals("موتورسیکلت کلیک", result.title)
        assertEquals("215", result.plateFirst2)
        assertEquals("موتور", result.plateLetter)
        assertEquals("48963", result.plateLast3)
        assertEquals(VehicleEntity.TYPE_MOTORCYCLE, result.vehicleType)
        assertEquals(6.0, result.tankCapacity, 0.1)
    }

    @Test
    fun testParseDirectPlatePattern() {
        val platePayload = "24ب852-68"
        val result = VehicleQrParser.parse(platePayload)

        assertEquals("24", result.plateFirst2)
        assertEquals("ب", result.plateLetter)
        assertEquals("852", result.plateLast3)
        assertEquals("68", result.plateCityCode)
    }
}
