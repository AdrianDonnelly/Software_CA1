using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using AutoParts.Data;
using AutoParts.Models;

namespace AutoParts.Controllers;

[Route("api/[controller]")]
[ApiController]
public class AutoPartsController : ControllerBase
{
    private readonly AutoPartsDbContext _context;

    public AutoPartsController(AutoPartsDbContext context)
    {
        _context = context;
    }

    // GET: api/AutoParts
    [HttpGet]
    public async Task<ActionResult<IEnumerable<AutoPart>>> GetAutoParts()
    {
        return await _context.AutoParts
            .Include(p => p.Vehicle)
            .ToListAsync();
    }

    // GET: api/AutoParts/5
    [HttpGet("{id}")]
    public async Task<ActionResult<AutoPart>> GetAutoPart(int id)
    {
        var autoPart = await _context.AutoParts
            .Include(p => p.Vehicle)
            .FirstOrDefaultAsync(p => p.PartId == id);

        if (autoPart == null)
        {
            return NotFound();
        }

        return autoPart;
    }

    // GET: api/AutoParts/search?query=brake
    [HttpGet("search")]
    public async Task<ActionResult<IEnumerable<AutoPart>>> SearchParts([FromQuery] string query)
    {
        if (string.IsNullOrWhiteSpace(query))
        {
            return await GetAutoParts();
        }

        var parts = await _context.AutoParts
            .Include(p => p.Vehicle)
            .Where(p => p.Name.Contains(query) || 
                       p.PartNumber.Contains(query) ||
                       p.Manufacturer.Contains(query))
            .ToListAsync();

        return parts;
    }

    // GET: api/AutoParts/category/Brakes
    [HttpGet("category/{category}")]
    public async Task<ActionResult<IEnumerable<AutoPart>>> GetPartsByCategory(string category)
    {
        var parts = await _context.AutoParts
            .Include(p => p.Vehicle)
            .Where(p => p.Category == category)
            .ToListAsync();

        return parts;
    }

    // GET: api/AutoParts/categories
    [HttpGet("categories")]
    public async Task<ActionResult<IEnumerable<string>>> GetCategories()
    {
        return await _context.AutoParts
            .Select(p => p.Category)
            .Distinct()
            .ToListAsync();
    }

    // GET: api/AutoParts/vehicle/1
    [HttpGet("vehicle/{vehicleId}")]
    public async Task<ActionResult<IEnumerable<AutoPart>>> GetPartsByVehicle(int vehicleId)
    {
        var parts = await _context.AutoParts
            .Include(p => p.Vehicle)
            .Where(p => p.VehicleId == vehicleId)
            .ToListAsync();

        return parts;
    }

    // POST: api/AutoParts
    [HttpPost]
    public async Task<ActionResult<AutoPart>> PostAutoPart(AutoPart autoPart)
    {
        _context.AutoParts.Add(autoPart);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetAutoPart), new { id = autoPart.PartId }, autoPart);
    }

    // PUT: api/AutoParts/5
    [HttpPut("{id}")]
    public async Task<IActionResult> PutAutoPart(int id, AutoPart autoPart)
    {
        if (id != autoPart.PartId)
        {
            return BadRequest();
        }

        _context.Entry(autoPart).State = EntityState.Modified;

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateConcurrencyException)
        {
            if (!AutoPartExists(id))
            {
                return NotFound();
            }
            throw;
        }

        return NoContent();
    }

    // DELETE: api/AutoParts/5
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteAutoPart(int id)
    {
        var autoPart = await _context.AutoParts.FindAsync(id);
        if (autoPart == null)
        {
            return NotFound();
        }

        _context.AutoParts.Remove(autoPart);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    private bool AutoPartExists(int id)
    {
        return _context.AutoParts.Any(e => e.PartId == id);
    }
}