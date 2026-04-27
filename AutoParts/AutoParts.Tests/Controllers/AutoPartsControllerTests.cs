using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using AutoParts.Controllers;
using AutoParts.Models;
using AutoParts.Tests.Helpers;
using Xunit;

namespace AutoParts.Tests.Controllers;

public class AutoPartsControllerTests
{
    private AutoPartsController CreateController(string dbName)
    {
        var context = TestHelpers.CreateInMemoryContext(dbName);
        return new AutoPartsController(context);
    }

    private async Task<AutoPartsController> CreateControllerWithData(string dbName)
    {
        var context = TestHelpers.CreateInMemoryContext(dbName);

        context.Vehicles.AddRange(
            TestHelpers.MakeVehicle(1),
            TestHelpers.MakeVehicle(2, "Ford", "F-150", 2021)
        );

        context.AutoParts.AddRange(
            TestHelpers.MakePart(1, 1, "Brakes", "Front Brake Pads", "TYT-BRK-001", "Bosch"),
            TestHelpers.MakePart(2, 1, "Engine", "Oil Filter", "TYT-ENG-001", "Toyota OEM"),
            TestHelpers.MakePart(3, 2, "Brakes", "Brake Rotors", "FRD-BRK-001", "ACDelco")
        );

        await context.SaveChangesAsync();
        return new AutoPartsController(context);
    }

    // GET /api/autoparts

    [Fact]
    public async Task GetAutoParts_ReturnsAllParts()
    {
        var controller = await CreateControllerWithData(nameof(GetAutoParts_ReturnsAllParts));

        var result = await controller.GetAutoParts();

        var parts = Assert.IsAssignableFrom<IEnumerable<AutoPart>>(result.Value);
        Assert.Equal(3, parts.Count());
    }

    [Fact]
    public async Task GetAutoParts_ReturnsEmptyList_WhenNoParts()
    {
        var controller = CreateController(nameof(GetAutoParts_ReturnsEmptyList_WhenNoParts));

        var result = await controller.GetAutoParts();

        Assert.Empty(result.Value!);
    }

    [Fact]
    public async Task GetAutoParts_IncludesVehicle()
    {
        var controller = await CreateControllerWithData(nameof(GetAutoParts_IncludesVehicle));

        var result = await controller.GetAutoParts();

        Assert.All(result.Value!, p => Assert.NotNull(p.Vehicle));
    }

    // GET /api/autoparts/{id}

    [Fact]
    public async Task GetAutoPart_ReturnsCorrectPart()
    {
        var controller = await CreateControllerWithData(nameof(GetAutoPart_ReturnsCorrectPart));

        var result = await controller.GetAutoPart(1);

        Assert.Equal(1, result.Value!.PartId);
        Assert.Equal("Front Brake Pads", result.Value.Name);
    }

    [Fact]
    public async Task GetAutoPart_ReturnsNotFound_WhenIdDoesNotExist()
    {
        var controller = await CreateControllerWithData(nameof(GetAutoPart_ReturnsNotFound_WhenIdDoesNotExist));

        var result = await controller.GetAutoPart(999);

        Assert.IsType<NotFoundResult>(result.Result);
    }

    [Fact]
    public async Task GetAutoPart_IncludesVehicle()
    {
        var controller = await CreateControllerWithData(nameof(GetAutoPart_IncludesVehicle));

        var result = await controller.GetAutoPart(1);

        Assert.NotNull(result.Value!.Vehicle);
        Assert.Equal("Toyota", result.Value.Vehicle!.Make);
    }

    // GET /api/autoparts/search

    [Fact]
    public async Task SearchParts_WithEmptyQuery_ReturnsAllParts()
    {
        var controller = await CreateControllerWithData(nameof(SearchParts_WithEmptyQuery_ReturnsAllParts));

        var result = await controller.SearchParts("");

        Assert.Equal(3, result.Value!.Count());
    }

    [Fact]
    public async Task SearchParts_WithWhitespaceQuery_ReturnsAllParts()
    {
        var controller = await CreateControllerWithData(nameof(SearchParts_WithWhitespaceQuery_ReturnsAllParts));

        var result = await controller.SearchParts("   ");

        Assert.Equal(3, result.Value!.Count());
    }

    [Fact]
    public async Task SearchParts_FiltersByName()
    {
        var controller = await CreateControllerWithData(nameof(SearchParts_FiltersByName));

        var result = await controller.SearchParts("Oil");

        var parts = result.Value!.ToList();
        Assert.Single(parts);
        Assert.Equal("Oil Filter", parts[0].Name);
    }

    [Fact]
    public async Task SearchParts_FiltersByPartNumber()
    {
        var controller = await CreateControllerWithData(nameof(SearchParts_FiltersByPartNumber));

        var result = await controller.SearchParts("TYT-BRK");

        var parts = result.Value!.ToList();
        Assert.Single(parts);
        Assert.Equal("TYT-BRK-001", parts[0].PartNumber);
    }

    [Fact]
    public async Task SearchParts_FiltersByManufacturer()
    {
        var controller = await CreateControllerWithData(nameof(SearchParts_FiltersByManufacturer));

        var result = await controller.SearchParts("ACDelco");

        var parts = result.Value!.ToList();
        Assert.Single(parts);
        Assert.Equal("ACDelco", parts[0].Manufacturer);
    }

    [Fact]
    public async Task SearchParts_ReturnsEmpty_WhenNoMatch()
    {
        var controller = await CreateControllerWithData(nameof(SearchParts_ReturnsEmpty_WhenNoMatch));

        var result = await controller.SearchParts("xyzNotExist");

        Assert.Empty(result.Value!);
    }

    // GET /api/autoparts/category/{category}

    [Fact]
    public async Task GetPartsByCategory_ReturnsMatchingParts()
    {
        var controller = await CreateControllerWithData(nameof(GetPartsByCategory_ReturnsMatchingParts));

        var result = await controller.GetPartsByCategory("Brakes");

        var parts = result.Value!.ToList();
        Assert.Equal(2, parts.Count);
        Assert.All(parts, p => Assert.Equal("Brakes", p.Category));
    }

    [Fact]
    public async Task GetPartsByCategory_ReturnsEmpty_WhenCategoryNotFound()
    {
        var controller = await CreateControllerWithData(nameof(GetPartsByCategory_ReturnsEmpty_WhenCategoryNotFound));

        var result = await controller.GetPartsByCategory("Suspension");

        Assert.Empty(result.Value!);
    }

    // GET /api/autoparts/categories

    [Fact]
    public async Task GetCategories_ReturnsDistinctCategories()
    {
        var controller = await CreateControllerWithData(nameof(GetCategories_ReturnsDistinctCategories));

        var result = await controller.GetCategories();

        var categories = result.Value!.ToList();
        Assert.Equal(2, categories.Count);
        Assert.Contains("Brakes", categories);
        Assert.Contains("Engine", categories);
    }

    [Fact]
    public async Task GetCategories_ReturnsEmpty_WhenNoParts()
    {
        var controller = CreateController(nameof(GetCategories_ReturnsEmpty_WhenNoParts));

        var result = await controller.GetCategories();

        Assert.Empty(result.Value!);
    }

    // GET /api/autoparts/vehicle/{vehicleId}

    [Fact]
    public async Task GetPartsByVehicle_ReturnsPartsForVehicle()
    {
        var controller = await CreateControllerWithData(nameof(GetPartsByVehicle_ReturnsPartsForVehicle));

        var result = await controller.GetPartsByVehicle(1);

        var parts = result.Value!.ToList();
        Assert.Equal(2, parts.Count);
        Assert.All(parts, p => Assert.Equal(1, p.VehicleId));
    }

    [Fact]
    public async Task GetPartsByVehicle_ReturnsEmpty_WhenVehicleHasNoParts()
    {
        var controller = await CreateControllerWithData(nameof(GetPartsByVehicle_ReturnsEmpty_WhenVehicleHasNoParts));

        var result = await controller.GetPartsByVehicle(999);

        Assert.Empty(result.Value!);
    }

    // POST /api/autoparts

    [Fact]
    public async Task PostAutoPart_CreatesPartAndReturns201()
    {
        var context = TestHelpers.CreateInMemoryContext(nameof(PostAutoPart_CreatesPartAndReturns201));
        context.Vehicles.Add(TestHelpers.MakeVehicle(1));
        await context.SaveChangesAsync();
        var controller = new AutoPartsController(context);
        TestHelpers.SetAdminContext(controller);

        var result = await controller.PostAutoPart(TestHelpers.MakePart(0, 1, "Engine", "Timing Belt", "TEST-NEW-001", "Gates"));

        var created = Assert.IsType<CreatedAtActionResult>(result.Result);
        var part = Assert.IsType<AutoPart>(created.Value);
        Assert.Equal("Timing Belt", part.Name);
    }

    [Fact]
    public async Task PostAutoPart_PartIsPersisted()
    {
        var context = TestHelpers.CreateInMemoryContext(nameof(PostAutoPart_PartIsPersisted));
        context.Vehicles.Add(TestHelpers.MakeVehicle(1));
        await context.SaveChangesAsync();
        var controller = new AutoPartsController(context);
        TestHelpers.SetAdminContext(controller);

        await controller.PostAutoPart(TestHelpers.MakePart(0, 1, "Engine", "Belt", "BELT-001", "Gates"));

        Assert.Equal(1, await context.AutoParts.CountAsync());
    }

    // PUT /api/autoparts/{id}

    [Fact]
    public async Task PutAutoPart_ReturnsNoContent_WhenSuccessful()
    {
        var context = TestHelpers.CreateInMemoryContext(nameof(PutAutoPart_ReturnsNoContent_WhenSuccessful));
        context.Vehicles.Add(TestHelpers.MakeVehicle(1));
        context.AutoParts.Add(TestHelpers.MakePart(1, 1));
        await context.SaveChangesAsync();
        context.ChangeTracker.Clear();
        var controller = new AutoPartsController(context);

        var updated = TestHelpers.MakePart(1, 1);
        updated.Name = "Updated Pads";
        var result = await controller.PutAutoPart(1, updated);

        Assert.IsType<NoContentResult>(result);
    }

    [Fact]
    public async Task PutAutoPart_ReturnsBadRequest_WhenIdMismatch()
    {
        var controller = await CreateControllerWithData(nameof(PutAutoPart_ReturnsBadRequest_WhenIdMismatch));

        var result = await controller.PutAutoPart(1, TestHelpers.MakePart(5, 1));

        Assert.IsType<BadRequestResult>(result);
    }

    // DELETE /api/autoparts/{id}

    [Fact]
    public async Task DeleteAutoPart_ReturnsNoContent_WhenSuccessful()
    {
        var controller = await CreateControllerWithData(nameof(DeleteAutoPart_ReturnsNoContent_WhenSuccessful));
        TestHelpers.SetAdminContext(controller);

        var result = await controller.DeleteAutoPart(1);

        Assert.IsType<NoContentResult>(result);
    }

    [Fact]
    public async Task DeleteAutoPart_RemovesPartFromDb()
    {
        var context = TestHelpers.CreateInMemoryContext(nameof(DeleteAutoPart_RemovesPartFromDb));
        context.Vehicles.Add(TestHelpers.MakeVehicle(1));
        context.AutoParts.Add(TestHelpers.MakePart(1, 1));
        await context.SaveChangesAsync();
        var controller = new AutoPartsController(context);
        TestHelpers.SetAdminContext(controller);

        await controller.DeleteAutoPart(1);

        Assert.Equal(0, await context.AutoParts.CountAsync());
    }

    [Fact]
    public async Task DeleteAutoPart_ReturnsNotFound_WhenIdDoesNotExist()
    {
        var controller = await CreateControllerWithData(nameof(DeleteAutoPart_ReturnsNotFound_WhenIdDoesNotExist));
        TestHelpers.SetAdminContext(controller);

        var result = await controller.DeleteAutoPart(999);

        Assert.IsType<NotFoundResult>(result);
    }
}
