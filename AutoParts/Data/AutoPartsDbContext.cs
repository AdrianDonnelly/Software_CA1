using Microsoft.EntityFrameworkCore;
using AutoParts.Models;

namespace AutoParts.Data;

public class AutoPartsDbContext : DbContext
{
    public AutoPartsDbContext(DbContextOptions<AutoPartsDbContext> options)
        : base(options)
    {
    }

    public DbSet<Vehicle> Vehicles { get; set; }
    public DbSet<AutoPart> AutoParts { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<AutoPart>()
            .HasOne(p => p.Vehicle)
            .WithMany(v => v.AutoParts)
            .HasForeignKey(p => p.VehicleId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<AutoPart>()
            .HasIndex(p => p.PartNumber)
            .IsUnique();

        modelBuilder.Entity<Vehicle>()
            .HasIndex(v => new { v.Make, v.Model, v.Year });

        SeedData(modelBuilder);
    }

    private void SeedData(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Vehicle>().HasData(
            new Vehicle { VehicleId = 1, Make = "Toyota", Model = "Corolla", Year = 2020, EngineType = "1.8L I4", Category = "Sedan", ImageUrl = "https://images.unsplash.com/photo-1623869675781-80aa31012a5a?w=400" },
            new Vehicle { VehicleId = 2, Make = "Ford", Model = "F-150", Year = 2021, EngineType = "3.5L V6", Category = "Truck", ImageUrl = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=400" },
            new Vehicle { VehicleId = 3, Make = "BMW", Model = "X5", Year = 2022, EngineType = "3.0L I6 Turbo", Category = "SUV", ImageUrl = "https://images.unsplash.com/photo-1555215695-1a1c7e8fb0a7?w=400" },
            new Vehicle { VehicleId = 4, Make = "Honda", Model = "Civic", Year = 2019, EngineType = "2.0L I4", Category = "Sedan", ImageUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=400" },
            new Vehicle { VehicleId = 5, Make = "Tesla", Model = "Model 3", Year = 2023, EngineType = "Electric", Category = "Sedan", ImageUrl = "https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=400" }
        );

        modelBuilder.Entity<AutoPart>().HasData(
            new AutoPart { PartId = 1, PartNumber = "TYT-BRK-001", Name = "Front Brake Pads", Category = "Brakes", Manufacturer = "Bosch", Price = 89.99m, StockQuantity = 25, VehicleId = 1, Description = "Premium ceramic brake pads", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=300" },
            new AutoPart { PartId = 2, PartNumber = "TYT-ENG-001", Name = "Oil Filter", Category = "Engine", Manufacturer = "Toyota OEM", Price = 12.99m, StockQuantity = 100, VehicleId = 1, Description = "Genuine Toyota oil filter", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1625047509248-ec889cbff17f?w=300" },
            new AutoPart { PartId = 3, PartNumber = "TYT-SUSP-001", Name = "Front Struts", Category = "Suspension", Manufacturer = "KYB", Price = 245.00m, StockQuantity = 10, VehicleId = 1, Description = "Gas-charged suspension struts", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?w=300" },
            
            new AutoPart { PartId = 4, PartNumber = "FRD-ENG-002", Name = "Air Filter", Category = "Engine", Manufacturer = "K&N", Price = 45.99m, StockQuantity = 50, VehicleId = 2, Description = "High-flow air filter", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1625047509248-ec889cbff17f?w=300" },
            new AutoPart { PartId = 5, PartNumber = "FRD-SUSP-001", Name = "Shock Absorber", Category = "Suspension", Manufacturer = "Monroe", Price = 189.99m, StockQuantity = 15, VehicleId = 2, Description = "Heavy-duty shock", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?w=300" },
            new AutoPart { PartId = 6, PartNumber = "FRD-BRK-003", Name = "Brake Rotors", Category = "Brakes", Manufacturer = "ACDelco", Price = 159.99m, StockQuantity = 20, VehicleId = 2, Description = "Vented brake rotors", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=300" },
            
            new AutoPart { PartId = 7, PartNumber = "BMW-ELEC-001", Name = "Headlight Assembly", Category = "Electrical", Manufacturer = "BMW OEM", Price = 450.00m, StockQuantity = 8, VehicleId = 3, Description = "LED headlight", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?w=300" },
            new AutoPart { PartId = 8, PartNumber = "BMW-BRK-002", Name = "Brake Rotors", Category = "Brakes", Manufacturer = "Brembo", Price = 320.00m, StockQuantity = 12, VehicleId = 3, Description = "Performance rotors", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=300" },
            
            new AutoPart { PartId = 9, PartNumber = "HND-ENG-003", Name = "Spark Plugs", Category = "Engine", Manufacturer = "NGK", Price = 28.99m, StockQuantity = 75, VehicleId = 4, Description = "Iridium spark plugs", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1625047509248-ec889cbff17f?w=300" },
            new AutoPart { PartId = 10, PartNumber = "HND-BRK-004", Name = "Brake Pads", Category = "Brakes", Manufacturer = "Akebono", Price = 65.00m, StockQuantity = 30, VehicleId = 4, Description = "Ceramic pads", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=300" },
            
            new AutoPart { PartId = 11, PartNumber = "TSL-ELEC-002", Name = "Cabin Air Filter", Category = "Electrical", Manufacturer = "Tesla OEM", Price = 35.00m, StockQuantity = 40, VehicleId = 5, Description = "HEPA filter", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1625047509248-ec889cbff17f?w=300" },
            new AutoPart { PartId = 12, PartNumber = "TSL-BRK-005", Name = "Brake Pads", Category = "Brakes", Manufacturer = "Tesla OEM", Price = 195.00m, StockQuantity = 18, VehicleId = 5, Description = "Regenerative brake pads", Condition = "New", ImageUrl = "https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=300" }
        );
    }
}