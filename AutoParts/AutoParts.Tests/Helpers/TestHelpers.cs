using System.Security.Claims;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using AutoParts.Data;
using AutoParts.Models;

namespace AutoParts.Tests.Helpers;

public static class TestHelpers
{
    public static AutoPartsDbContext CreateInMemoryContext(string dbName)
    {
        var options = new DbContextOptionsBuilder<AutoPartsDbContext>()
            .UseInMemoryDatabase(dbName)
            .Options;

        return new AutoPartsDbContext(options);
    }

    public static void SetAdminContext(ControllerBase controller)
    {
        var claims = new List<Claim>
        {
            new Claim("app_metadata", "{\"role\":\"admin\"}")
        };
        var identity = new ClaimsIdentity(claims, "TestAuth");
        controller.ControllerContext = new ControllerContext
        {
            HttpContext = new DefaultHttpContext { User = new ClaimsPrincipal(identity) }
        };
    }

    public static void SetNonAdminContext(ControllerBase controller, string? userId = "user-123")
    {
        var claims = new List<Claim>();
        if (userId != null)
            claims.Add(new Claim(ClaimTypes.NameIdentifier, userId));
        var identity = new ClaimsIdentity(claims, "TestAuth");
        controller.ControllerContext = new ControllerContext
        {
            HttpContext = new DefaultHttpContext { User = new ClaimsPrincipal(identity) }
        };
    }

    public static Vehicle MakeVehicle(int id = 1, string make = "Toyota", string model = "Corolla", int year = 2020) =>
        new Vehicle
        {
            VehicleId = id,
            Make = make,
            Model = model,
            Year = year,
            EngineType = "1.8L I4",
            Category = "Sedan"
        };

    public static AutoPart MakePart(int partId, int vehicleId, string category = "Brakes", string name = "Brake Pads",
        string partNumber = "TEST-001", string manufacturer = "Bosch") =>
        new AutoPart
        {
            PartId = partId,
            VehicleId = vehicleId,
            Name = name,
            PartNumber = partNumber,
            Category = category,
            Manufacturer = manufacturer,
            Price = 99.99m,
            StockQuantity = 10,
            Condition = "New"
        };
}
