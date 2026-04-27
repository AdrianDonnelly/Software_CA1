using System.Security.Claims;
using System.Text.Json;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using AutoParts.Data;
using AutoParts.Models;

namespace AutoParts.Controllers;

[Route("api/[controller]")]
[ApiController]
public class VehiclesController : ControllerBase
{
    private readonly AutoPartsDbContext _context;

    public VehiclesController(AutoPartsDbContext context)
    {
        _context = context;
    }

    private bool IsAdmin()
    {
        var appMetadata = User.FindFirst("app_metadata")?.Value;
        if (appMetadata == null) return false;
        try
        {
            var metadata = JsonSerializer.Deserialize<JsonElement>(appMetadata);
            return metadata.TryGetProperty("role", out var role) && role.GetString() == "admin";
        }
        catch { return false; }
    }

    // GET: api/Vehicles
    [HttpGet]
    public async Task<ActionResult<IEnumerable<Vehicle>>> GetVehicles()
    {
        return await _context.Vehicles
            .Include(v => v.AutoParts)
            .ToListAsync();
    }

    // GET: api/Vehicles/5
    [HttpGet("{id}")]
    public async Task<ActionResult<Vehicle>> GetVehicle(int id)
    {
        var vehicle = await _context.Vehicles
            .Include(v => v.AutoParts)
            .FirstOrDefaultAsync(v => v.VehicleId == id);

        if (vehicle == null)
            return NotFound();

        return vehicle;
    }

    // GET: api/Vehicles/5/parts
    [HttpGet("{id}/parts")]
    public async Task<ActionResult<IEnumerable<AutoPart>>> GetVehicleParts(int id)
    {
        var vehicle = await _context.Vehicles
            .Include(v => v.AutoParts)
            .FirstOrDefaultAsync(v => v.VehicleId == id);

        if (vehicle == null)
            return NotFound();

        return Ok(vehicle.AutoParts);
    }

    // GET: api/Vehicles/makes
    [HttpGet("makes")]
    public async Task<ActionResult<IEnumerable<string>>> GetMakes()
    {
        return await _context.Vehicles
            .Select(v => v.Make)
            .Distinct()
            .ToListAsync();
    }

    // POST: api/Vehicles — admin only
    [HttpPost]
    [Authorize]
    public async Task<ActionResult<Vehicle>> PostVehicle(Vehicle vehicle)
    {
        if (!IsAdmin())
            return Forbid();

        _context.Vehicles.Add(vehicle);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetVehicle), new { id = vehicle.VehicleId }, vehicle);
    }

    // PUT: api/Vehicles/5 — admin only
    [HttpPut("{id}")]
    [Authorize]
    public async Task<IActionResult> PutVehicle(int id, Vehicle vehicle)
    {
        if (!IsAdmin())
            return Forbid();

        if (id != vehicle.VehicleId)
            return BadRequest();

        _context.Entry(vehicle).State = EntityState.Modified;

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateConcurrencyException)
        {
            if (!VehicleExists(id))
                return NotFound();
            throw;
        }

        return NoContent();
    }

    // DELETE: api/Vehicles/5 — admin only
    [HttpDelete("{id}")]
    [Authorize]
    public async Task<IActionResult> DeleteVehicle(int id)
    {
        if (!IsAdmin())
            return Forbid();

        var vehicle = await _context.Vehicles.FindAsync(id);
        if (vehicle == null)
            return NotFound();

        _context.Vehicles.Remove(vehicle);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    private bool VehicleExists(int id)
    {
        return _context.Vehicles.Any(e => e.VehicleId == id);
    }
}
